package services.plan

import java.util.UUID

import models.plan.PlanError

object MySqlParseService extends PlanParseService("mysql") {
  override def parse(sql: String, queryId: UUID, plan: String) = {
    Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = "NotSupported",
      message = "No plan support for MySQL currently."
    ))
  }
}
