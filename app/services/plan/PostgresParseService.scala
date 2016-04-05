package services.plan

import java.util.UUID

import models.plan.{ PlanError, PlanNode, PlanResult }
import upickle.Js
import upickle.json

import PostgresParseKeys._

object PostgresParseService extends PlanParseService("postgres") {
  override def parse(sql: String, queryId: UUID, plan: String) = {
    val json = upickle.json.read(plan)
    val ret = json match {
      case a: Js.Arr => if (a.value.length == 1) {
        a.value.headOption match {
          case Some(x: Js.Obj) => x.value match {
            case planEl if planEl.headOption.map(_._1).contains("Plan") => parsePlan(sql, queryId, planEl.head._2)
            case v => throw new IllegalArgumentException("Expected single element \"Plan\", found [" + v.map(_._1).mkString(", ") + "].")
          }
          case x => throw new IllegalArgumentException(s"Array contains [${a.value.length}] elements, and the head is of type [$x].")
        }
      } else {
        throw new IllegalStateException(s"Source has [${a.value.length}] elements, which is more than the expected one.")
      }
      case _ => throw new IllegalStateException("Not a Json array.")
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

  private[this] def nodeFor(jsVal: Js.Value): PlanNode = jsVal match {
    case o: Js.Obj =>
      val params = o.value.toMap
      //log.info(params.map(x => x._1 + " = " + x._2.toString).mkString("\n"))

      val children = params.get(keyPlans).map {
        case plans: Js.Arr => plans.value.map(nodeFor)
        case x => throw new IllegalStateException(s"Unable to parse plans from [$x]")
      }.getOrElse(Nil)

      val props = params.filter { p =>
        p._1 == keyPlans
      }.map(p => p._1 -> json.write(p._2))

      PlanNode(
        title = params.get(keyNodeType).map(_.asInstanceOf[Js.Str].value).getOrElse("?"),
        nodeType = "?",
        properties = props,
        children = children
      )
    case x => throw new IllegalStateException(s"Invalid node type [${x.getClass.getName}]")
  }
}
