package services.plan

import java.util.UUID

import models.plan.{ PlanError, PlanNode, PlanResult }
import upickle.{ Js, json }

import scala.util.control.NonFatal

object MySqlParseService extends PlanParseService("mysql") {
  override def parse(sql: String, queryId: UUID, plan: String, startMs: Long) = {
    val json = upickle.json.read(plan)
    json match {
      case o: Js.Obj => parsePlan(sql, queryId, o, startMs)
      case _ => throw new IllegalStateException("Not a Json object.")
    }
  }

  private[this] def parsePlan(sql: String, queryId: UUID, plan: Js.Value, startMs: Long) = try {
    Right(PlanResult(
      queryId = queryId,
      action = "Action",
      sql = sql,
      raw = json.write(plan, 2),
      node = nodeFor(plan),
      occurred = startMs
    ))
  } catch {
    case NonFatal(x) => Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = x.getClass.getSimpleName,
      message = x.getMessage,
      raw = Some(json.write(plan, 2)),
      occurred = startMs
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
        potentialNodeType.headOption.getOrElse(throw new IllegalStateException())
      } else {
        throw new IllegalStateException(s"Multiple params for node type: [${potentialNodeType.keys.toSeq.sorted.mkString(", ")}].")
      }

      nodeType._1 match {
        case "nested_loop" => parseNestedLoop(nodeType._2)
        case "union_result" => parseUnionResult(nodeType._2)
        case "table" => parseTable(nodeType._2)
        case x => throw new IllegalStateException(s"Unable to parse children from node type [$x].")
      }
    case x => throw new IllegalStateException(s"Invalid query block type [${x.getClass.getSimpleName}].")
  }

  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def parseNestedLoop(el: Js.Value) = PlanNode(
    title = "Nested Loop",
    nodeType = "Nested Loop",
    children = el match {
    case a: Js.Arr => a.value.map { v =>
      parseTable(v match {
        case o: Js.Obj => o.value.headOption.getOrElse(throw new IllegalStateException("Missing data."))._2
        case x => throw new IllegalStateException(x.toString)
      })
    }
    case _ => Nil
  }
  )

  private[this] def parseUnionResult(el: Js.Value) = PlanNode(
    title = "Union Result",
    nodeType = "Union Result",
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
}
