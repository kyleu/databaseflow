package services.plan

import java.util.UUID

import models.plan.{ PlanError, PlanNode, PlanResult }
import upickle.{ Js, json }

import scala.util.control.NonFatal

object MySqlParseService extends PlanParseService("mysql") {
  override def parse(sql: String, queryId: UUID, plan: String) = {
    val json = upickle.json.read(plan)
    val ret = json match {
      case o: Js.Obj => parsePlan(sql, queryId, o)
      case _ => throw new IllegalStateException("Not a Json object.")
    }
    ret match {
      case Left(err) => println(upickle.default.write(err, 2))
      case Right(result) => println(upickle.default.write(result.node, 2))
    }
    ret
  }

  private[this] def parsePlan(sql: String, queryId: UUID, plan: Js.Value) = try {
    Right(PlanResult(
      queryId = queryId,
      name = "Test Plan",
      action = "Action",
      sql = sql,
      raw = json.write(plan, 2),
      node = nodeFor(plan)
    ))
  } catch {
    case NonFatal(x) => Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = x.getClass.getSimpleName,
      message = x.getMessage,
      raw = Some(json.write(plan, 2))
    ))
  }

  private[this] def nodeFor(json: Js.Value): PlanNode = json match {
    case o: Js.Obj =>
      val params = o.value.toMap
      params.get("query_block") match {
        case Some(el) => parseQueryBlock(1, el)
        case _ => throw new IllegalStateException("Missing [query_block] element.")
      }
    case x => throw new IllegalStateException(s"Invalid node type [${x.getClass.getName}].")
  }

  private[this] def parseQueryBlock(depth: Int, json: Js.Value) = json match {
    case o: Js.Obj =>
      val params = o.value.toMap

      val potentialNodeType = params.filterNot(_._1 == "select_id")
      val nodeType = if (potentialNodeType.size == 1) {
        potentialNodeType.head
      } else {
        throw new IllegalStateException(s"Multiple params for node type: [${potentialNodeType.keys.toSeq.sorted.mkString(", ")}].")
      }

      nodeType._1 match {
        case "nested_loop" => parseNestedLoop(depth, nodeType._2)
        case "union_result" => parseUnionResult(depth, nodeType._2)
        case "table" => parseTable(depth, nodeType._2)
        case x => throw new IllegalStateException(s"Unable to parse children from node type [$x].")
      }
    case x => throw new IllegalStateException(s"Invalid query block type [${x.getClass.getSimpleName}].")
  }

  private[this] def parseNestedLoop(depth: Int, el: Js.Value) = PlanNode(
    title = "Nested Loop " + depth,
    nodeType = "Nested Loop",
    children = el match {
    case a: Js.Arr => a.value.map(x => parseTable(depth + 1, x.asInstanceOf[Js.Obj].value.head._2))
    case _ => Nil
  }
  )

  private[this] def parseUnionResult(depth: Int, el: Js.Value) = PlanNode(
    title = "Union Result " + depth,
    nodeType = "Union Result",
    children = Nil
  )

  private[this] def parseTable(depth: Int, el: Js.Value) = {
    println("!!!" + el)
    val obj = (el match {
      case o: Js.Obj => o
      case x => throw new IllegalStateException(s"Table element is of type [${x.getClass.getSimpleName}].")
    }).value.toMap
    PlanNode(
      title = obj("table_name").asInstanceOf[Js.Str].value,
      nodeType = obj("access_type").asInstanceOf[Js.Str].value,
      children = Nil
    )
  }
}
