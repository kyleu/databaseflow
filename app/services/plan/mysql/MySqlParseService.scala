package services.plan.mysql

import java.util.UUID

import models.plan.{PlanError, PlanNode, PlanResult}
import services.plan.PlanParseService
import upickle.{Js, json}

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
    case NonFatal(x) =>
      log.warn("Error parsing MySQL query.", x)
      Left(PlanError(
        queryId = queryId,
        sql = sql,
        code = x.getClass.getSimpleName,
        message = x.getMessage,
        raw = Some(json.write(plan, 2)),
        occurred = startMs
      ))
  }

  private[this] def nodeFor(json: Js.Value): PlanNode = json.obj.get(MySqlParseKeys.keyQueryBlock) match {
    case Some(el) => MySqlQueryBlockParser.parseQueryBlock(el)
    case _ => throw new IllegalStateException(s"Missing [${MySqlParseKeys.keyQueryBlock}] element.")
  }
}
