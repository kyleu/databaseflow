package services.plan

import models.engine.DatabaseEngine
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }
import utils.Logging

object PlanParseService {
  def parse(sql: String, plan: String)(implicit engine: DatabaseEngine) = engine match {
    case PostgreSQL => PostgresParseService.parse(sql, plan)
    case MySQL => MySqlParseService.parse(sql, plan)
    case H2 => H2ParseService.parse(sql, plan)
  }
}

abstract class PlanParseService(name: String) extends Logging {
  def parse(sql: String, plan: String)

  def debug() = {
    log.info(s"Started [$name] plan parse service.")
  }
}
