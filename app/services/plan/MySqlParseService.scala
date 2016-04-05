package services.plan

import java.util.UUID

import models.plan.{ PlanError, PlanNode, PlanResult }
import upickle.{ Js, json }

object MySqlParseService extends PlanParseService("mysql") {
  override def parse(sql: String, queryId: UUID, plan: String) = {
    val json = upickle.json.read(plan)
    val ret = json match {
      case o: Js.Obj => parsePlan(sql, queryId, o)
      case _ => throw new IllegalStateException("Not a Json object.")
    }
    ret
  }

  private[this] def parsePlan(sql: String, queryId: UUID, plan: Js.Value) = plan match {
    case o: Js.Obj => Right(PlanResult(
      queryId = queryId,
      name = "Test Plan",
      action = "Action",
      sql = sql,
      raw = json.write(plan, 2),
      node = nodeFor(o)
    ))
    case x => Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = x.getClass.getSimpleName,
      message = s"Invalid JSON [${json.write(plan)}].",
      raw = Some(json.write(plan, 2))
    ))
  }

  private[this] def nodeFor(json: Js.Value): PlanNode = json match {
    case o: Js.Obj =>
      val params = o.value.toMap
      //log.info(params.map(x => x._1 + " = " + x._2.toString).mkString("\n"))

      val children = params.get("?").map {
        case plans: Js.Arr => plans.value.map(nodeFor)
        case x => throw new IllegalStateException(s"Unable to parse plans from [$x]")
      }.getOrElse(Nil)

      PlanNode(
        title = params.get("?").map(_.asInstanceOf[Js.Str].value).getOrElse("?"),
        nodeType = "?",
        children = children
      )
    case x => throw new IllegalStateException(s"Invalid node type [${x.getClass.getName}]")
  }
}
