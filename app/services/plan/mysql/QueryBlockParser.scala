package services.plan.mysql

import models.plan.PlanNode
import upickle.Js

object QueryBlockParser {
  def parseQueryBlock(depth: Int, json: Js.Value) = json match {
    case o: Js.Obj =>
      val params = o.value.toMap

      val potentialNodeType = params.filterNot(_._1 == "select_id")
      val nodeType = if (potentialNodeType.size == 1) {
        potentialNodeType.headOption.getOrElse(throw new IllegalStateException())
      } else {
        throw new IllegalStateException(s"Multiple params for node type: [${potentialNodeType.keys.toSeq.sorted.mkString(", ")}] at depth [$depth].")
      }

      nodeType._1 match {
        case "nested_loop" => parseNestedLoop(nodeType._2)
        case "union_result" => parseUnionResult(nodeType._2)
        case "table" => parseTable(nodeType._2)
        case "grouping_operation" => parseGroupingOperation(nodeType._2)
        case x => throw new IllegalStateException(s"Unable to parse children from node type [$x].")
      }
    case x => throw new IllegalStateException(s"Invalid query block type [${x.getClass.getSimpleName}].")
  }

  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def parseNestedLoop(el: Js.Value) = {
    val children = el match {
      case a: Js.Arr => a.value.map { v =>
        parseTable(v match {
          case o: Js.Obj => o.value.headOption.getOrElse(throw new IllegalStateException("Missing data."))._2
          case x => throw new IllegalStateException(x.toString)
        })
      }
      case _ => Nil
    }
    PlanNode(
      title = "Nested Loop",
      nodeType = "Nested Loop",
      children = children
    )
  }

  private[this] def parseUnionResult(el: Js.Value) = PlanNode(
    title = "Union Result",
    nodeType = el.toString,
    children = Nil
  )

  private[this] def parseTable(el: Js.Value) = {
    val obj = (el match {
      case o: Js.Obj => o
      case x => throw new IllegalStateException(s"Table element is of type [${x.getClass.getSimpleName}].")
    }).value.toMap
    PlanNode(
      title = obj("table_name") match {
      case s: Js.Str => s.value
      case x => throw new IllegalStateException(x.toString)
    },
      nodeType = obj("access_type") match {
      case s: Js.Str => s.value
      case x => throw new IllegalStateException(x.toString)
    },
      children = Nil
    )
  }

  private[this] def parseGroupingOperation(el: Js.Value) = {
    val obj = (el match {
      case o: Js.Obj => o
      case x => throw new IllegalStateException(s"Grouping operation element is of type [${x.getClass.getSimpleName}].")
    }).value.toMap
    PlanNode(
      title = "Grouping Operation",
      nodeType = "Grouping Operation",
      children = Seq(parseNestedLoop(obj("nested_loop")))
    )
  }
}
