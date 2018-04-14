package services.plan.mysql

import java.util.UUID

import models.plan.{PlanError, PlanNode, PlanResult}
import services.plan.PlanParseService
import util.JsonSerializers._

import scala.util.control.NonFatal

object MySqlParseService extends PlanParseService("mysql") {
  override def parse(sql: String, queryId: UUID, plan: String, startMs: Long) = {
    val json = parseJson(plan) match {
      case Right(x) => x
      case Left(x) => throw x
    }
    json match {
      case o if o.isObject => parsePlan(sql, queryId, o, startMs)
      case _ => throw new IllegalStateException("Not a Json object.")
    }
  }

  private[this] def parsePlan(sql: String, queryId: UUID, plan: Json, startMs: Long) = try {
    Right(PlanResult(
      queryId = queryId,
      action = "Action",
      sql = sql,
      raw = plan.spaces2,
      node = nodeFor(plan),
      occurred = startMs
    ))
  } catch {
    case NonFatal(x) =>
      log.warn("Error parsing MySQL query.", x)
      Left(PlanError(
        queryId = queryId,
        sql = sql,
        code = x.getClass.getSimpleName,
        message = x.getMessage,
        raw = Some(plan.spaces2),
        occurred = startMs
      ))
  }

  private[this] def nodeFor(json: Json): PlanNode = json.asObject.get(MySqlParseKeys.keyQueryBlock) match {
    case Some(el) => MySqlQueryBlockParser.parseQueryBlock(el)
    case _ => throw new IllegalStateException(s"Missing [${MySqlParseKeys.keyQueryBlock}] element.")
  }
}
