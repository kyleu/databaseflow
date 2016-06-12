package services.plan

import java.util.UUID

import models.plan.{PlanError, PlanResult}
import upickle.{Js, json}

object PostgresParseService extends PlanParseService("postgres") {
  override def parse(sql: String, queryId: UUID, plan: String, startMs: Long) = {
    val json = upickle.json.read(plan)
    val ret = json match {
      case a: Js.Arr => if (a.value.length == 1) {
        a.value.headOption match {
          case Some(x: Js.Obj) => x.value match {
            case planEl if planEl.headOption.map(_._1).contains("Plan") =>
              val plan = planEl.headOption.getOrElse(throw new IllegalStateException())._2
              parsePlan(sql, queryId, plan, startMs)
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

  private[this] def parsePlan(sql: String, queryId: UUID, plan: Js.Value, startMs: Long) = plan match {
    case o: Js.Obj => Right(PlanResult(
      queryId = queryId,
      action = "Action",
      sql = sql,
      raw = json.write(plan, 2),
      node = PostgresNodeParser.nodeFor(o),
      occurred = startMs
    ))
    case x => Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = x.getClass.getSimpleName,
      message = s"Invalid JSON [${json.write(plan)}].",
      raw = Some(json.write(plan, 2)),
      occurred = startMs
    ))
  }
}
