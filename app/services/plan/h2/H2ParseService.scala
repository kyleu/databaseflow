package services.plan.h2

import java.util.UUID

import models.plan.PlanError
import services.plan.PlanParseService

object H2ParseService extends PlanParseService("h2") {
  override def parse(sql: String, queryId: UUID, plan: String, startMs: Long) = {
    Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = "NotSupported",
      message = "No plan support for H2 currently.",
      occurred = startMs
    ))
  }
}
