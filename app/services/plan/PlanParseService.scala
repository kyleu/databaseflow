package services.plan

import models.engine.DatabaseEngine
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }
import models.plan.PlanResult
import models.query.QueryResult
import utils.Logging

object PlanParseService {
  def parse(sql: String, plan: String)(implicit engine: DatabaseEngine) = engine match {
    case PostgreSQL => PostgresParseService.parse(sql, plan)
    case MySQL => MySqlParseService.parse(sql, plan)
    case H2 => H2ParseService.parse(sql, plan)
  }

  def resultPlanString(result: (scala.Seq[QueryResult.Col], scala.Seq[scala.Seq[Option[Any]]]))(implicit engine: DatabaseEngine) = engine match {
    case PostgreSQL => result._2.map(_.head.map(_.toString).getOrElse("")).mkString("\n")
    case _ => throw new IllegalArgumentException("Parse result error.")
  }
}

abstract class PlanParseService(name: String) extends Logging {
  def parse(sql: String, plan: String): PlanResult

  def debug() = {
    log.info(s"Started [$name] plan parse service.")
  }
}
