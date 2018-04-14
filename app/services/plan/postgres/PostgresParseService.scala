package services.plan.postgres

import java.util.UUID

import models.plan.{PlanError, PlanResult}
import services.plan.PlanParseService
import util.JsonSerializers._

import scala.util.control.NonFatal

object PostgresParseService extends PlanParseService("postgres") {
  override def parse(sql: String, queryId: UUID, plan: String, startMs: Long) = {
    val json = parseJson(plan) match {
      case Right(x) => x
      case Left(x) => throw x
    }
    val ret = json match {
      case a if a.isArray => if (a.asArray.get.length == 1) {
        a.asArray.get.headOption match {
          case Some(x) if x.isObject => x.asObject.get.toMap match {
            case planEl if planEl.headOption.map(_._1).contains("Plan") =>
              val p = planEl.headOption.getOrElse(throw new IllegalStateException())._2
              parsePlan(sql, queryId, p, startMs)
            case v => throw new IllegalArgumentException("Expected single element \"Plan\", found [" + v.keys.mkString(", ") + "].")
          }
          case x => throw new IllegalArgumentException(s"Array contains [${a.asArray.get.length}] elements, and the head is of type [$x].")
        }
      } else {
        throw new IllegalStateException(s"Source has [${a.asArray.get.length}] elements, which is more than the expected one.")
      }
      case _ => throw new IllegalStateException("Not a Json array.")
    }
    ret
  }

  private[this] def parsePlan(sql: String, queryId: UUID, plan: Json, startMs: Long) = try {
    plan match {
      case o if o.isObject => Right(PlanResult(
        queryId = queryId,
        action = "Action",
        sql = sql,
        raw = plan.asJson.spaces2,
        node = PostgresNodeParser.nodeFor(o),
        occurred = startMs
      ))
      case x =>
        Left(PlanError(
          queryId = queryId,
          sql = sql,
          code = x.getClass.getSimpleName,
          message = s"Invalid JSON [${plan.asJson.spaces2}].",
          raw = Some(plan.asJson.spaces2),
          occurred = startMs
        ))
    }
  } catch {
    case NonFatal(x) =>
      log.warn("Error parsing Postgres query.", x)
      Left(PlanError(
        queryId = queryId,
        sql = sql,
        code = x.getClass.getSimpleName,
        message = x.getMessage,
        raw = Some(plan.asJson.spaces2),
        occurred = startMs
      ))
  }
}
