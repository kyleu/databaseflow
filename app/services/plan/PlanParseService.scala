package services.plan

import java.util.UUID

import models.engine.DatabaseEngine
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }
import models.plan.{ PlanError, PlanResult }
import models.queries.DynamicQuery
import utils.Logging

object PlanParseService {
  def parse(sql: String, queryId: UUID, plan: String, startMs: Long)(implicit engine: DatabaseEngine) = engine match {
    case PostgreSQL => PostgresParseService.parse(sql, queryId, plan, startMs)
    case MySQL => MySqlParseService.parse(sql, queryId, plan, startMs)
    case H2 => H2ParseService.parse(sql, queryId, plan, startMs)
  }

  def resultPlanString(result: DynamicQuery.Results)(implicit engine: DatabaseEngine) = engine match {
    case PostgreSQL => result.data.map(_.head.getOrElse("")).mkString("\n")
    case MySQL => result.data.map(_.head.getOrElse("")).mkString("\n")
    case _ => throw new IllegalArgumentException("Parse result error.")
  }
}

abstract class PlanParseService(name: String) extends Logging {
  def parse(sql: String, queryId: UUID, plan: String, startMs: Long): Either[PlanError, PlanResult]

  def debug() = {
    log.info(s"Started [$name] plan parse service.")
  }
}
