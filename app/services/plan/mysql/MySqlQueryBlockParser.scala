package services.plan.mysql

import models.plan.PlanNode
import upickle.Js
import upickle.Js.Value
import utils.JsonUtils

object MySqlQueryBlockParser {
  def parseQueryBlock(json: Js.Value): PlanNode = {
    val params = json.obj

    val potentialNodeType = params.filterNot(_._1 == "select_id")
    val nodeType = if (potentialNodeType.size == 1) {
      potentialNodeType.headOption.getOrElse(throw new IllegalStateException())
    } else {
      throw new IllegalStateException(s"Multiple params for node type: [${potentialNodeType.keys.toSeq.sorted.mkString(", ")}].")
    }

    nodeType._1 match {
      case "nested_loop" => parseNestedLoop(nodeType._2)
      case "union_result" => parseUnionResult(nodeType._2)
      case "table" => parseTable(nodeType._2)
      case "grouping_operation" => parseGroupingOperation(nodeType._2)
      case x => throw new IllegalStateException(s"Unable to parse children from node type [$x].")
    }
  }

  private[this] def parseNestedLoop(el: Js.Value) = {
    val children = el.arr.map(v => parseTable(v.obj(MySqlParseKeys.keyTable)))
    val rows = children.map(c => c.costs.actualRows.orElse(c.costs.estimatedCost).getOrElse(0)).product
    val costs = PlanNode.Costs(actualRows = if (rows != 0) { Some(rows) } else { None })
    PlanNode(title = "Nested Loop", nodeType = "Nested Loop", costs = costs, children = children)
  }

  def parseQuerySpecifications(v: Value) = {
    v.arr.map(_.obj(MySqlParseKeys.keyQueryBlock)).map(parseQueryBlock)
  }

  private[this] def parseUnionResult(el: Js.Value) = {
    val obj = el.obj

    val children = obj.get(MySqlParseKeys.keyQuerySpecifications).map(parseQuerySpecifications).getOrElse(Nil)

    val props = JsonUtils.toStringMap(obj).filterNot(_._1 == MySqlParseKeys.keyQuerySpecifications)
    val relation = props("table_name")
    PlanNode(title = "Union Result", nodeType = "Union Result", relation = Some(relation), properties = props, children = children)
  }

  private[this] def parseTable(el: Js.Value) = {
    val obj = el.obj
    val props = JsonUtils.toStringMap(obj)

    val subview = obj.get(MySqlParseKeys.keyMaterializedFromSubquery).map { s =>
      val v = s.obj.getOrElse(MySqlParseKeys.keyQueryBlock, throw new IllegalStateException("Missing query block."))
      parseQueryBlock(v)
    }

    val tableName = props.get(MySqlParseKeys.keyTableName)
    val message = props.get(MySqlParseKeys.keyMessage)
    val title = tableName.orElse(message).getOrElse {
      val msg = s"Missing key [${MySqlParseKeys.keyTableName}] or [${MySqlParseKeys.keyMessage}] from keys [${props.keys.mkString(", ")}]."
      throw new IllegalStateException(msg)
    }
    val relation = props.get(MySqlParseKeys.keyAttachedCondition)
    val nodeType = props.get(MySqlParseKeys.keyAccessType).orElse(message).getOrElse("Table")
    val rows = obj.get("rows").map(_.num.toInt)

    val costs = PlanNode.Costs(actualRows = rows)
    PlanNode(title = title, nodeType = nodeType, relation = relation, costs = costs, properties = props)
  }

  private[this] def parseGroupingOperation(el: Js.Value) = {
    val obj = el.obj
    PlanNode(
      title = "Grouping Operation",
      nodeType = "Grouping Operation",
      properties = JsonUtils.toStringMap(obj),
      children = Seq(parseNestedLoop(obj("nested_loop")))
    )
  }
}
