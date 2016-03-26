package services.plan

import java.util.UUID

import models.plan.{ PlanNode, PlanResult }
import upickle.Js
import upickle.json

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
      case _ => throw new IllegalStateException("Not a Json object.")
    }
    ret
  }

  private[this] def parsePlan(sql: String, queryId: UUID, plan: Js.Value): PlanResult = plan match {
    case o: Js.Obj =>
      val params = o.value.toMap

      log.info(params.map(x => x._1 + " = " + x._2.toString).mkString("\n"))
      PlanResult(
        queryId = queryId,
        name = "Test Plan",
        action = "Action",
        sql = sql,
        asText = json.write(plan, 2),
        node = PlanNode(title = "TODO", nodeType = "?")
      )
    case x => throw new IllegalStateException(x.toString)
  }
}
