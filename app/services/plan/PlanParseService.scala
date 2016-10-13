package services.plan

import java.util.UUID

import models.engine.DatabaseEngine._
import models.engine._
import models.plan.{PlanError, PlanResult}
import models.queries.dynamic.DynamicQuery
import services.plan.h2.H2ParseService
import services.plan.mysql.MySqlParseService
import services.plan.oracle.OracleParseService
import services.plan.postgres.PostgresParseService
import utils.Logging

object PlanParseService {
  def parse(sql: String, queryId: UUID, plan: String, startMs: Long)(implicit engine: DatabaseEngine) = engine match {
    case H2 => H2ParseService.parse(sql, queryId, plan, startMs)
    case MySQL => MySqlParseService.parse(sql, queryId, plan, startMs)
    case Oracle => OracleParseService.parse(sql, queryId, plan, startMs)
    case PostgreSQL => PostgresParseService.parse(sql, queryId, plan, startMs)
    case x => throw new IllegalStateException("Engine [] does not support execution plans.")
  }

  def resultPlanString(result: DynamicQuery.Results)(implicit engine: DatabaseEngine) = engine match {
    case MySQL => result.data.map(_.headOption.flatten.getOrElse("")).mkString("\n")
    case Oracle => result.data.map(_.headOption.flatten.getOrElse("")).mkString("\n")
    case PostgreSQL => result.data.map(_.headOption.flatten.getOrElse("")).mkString("\n")
    case _ => throw new IllegalArgumentException("Parse result error.")
  }
}

abstract class PlanParseService(name: String) extends Logging {
  def parse(sql: String, queryId: UUID, plan: String, startMs: Long): Either[PlanError, PlanResult]

  def debug() = {
    log.info(s"Started [$name] plan parse service.")
  }
}
