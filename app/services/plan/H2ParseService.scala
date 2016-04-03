package services.plan

import java.util.UUID

import models.plan.PlanError

object H2ParseService extends PlanParseService("h2") {
  override def parse(sql: String, queryId: UUID, plan: String) = {
    Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = "NotSupported",
      message = "No plan support for H2 currently."
    ))
  }
}
