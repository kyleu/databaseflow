package services.plan

import models.engine.DatabaseEngine
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }

object PlanParseService {
  def parse(sql: String, plan: String)(implicit engine: DatabaseEngine) = engine match {
    case PostgreSQL => PostgresParseService.parse(sql, plan)
    case MySQL => MySqlParseService.parse(sql, plan)
    case H2 => H2ParseService.parse(sql, plan)
  }
}
