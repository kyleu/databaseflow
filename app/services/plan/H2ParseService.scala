package services.plan

import java.util.UUID

import models.plan.{ PlanNode, PlanResult }

object H2ParseService extends PlanParseService("h2") {
  override def parse(sql: String, queryId: UUID, plan: String) = {
    PlanResult(
      queryId = queryId,
      name = "",
      action = "",
      sql = sql,
      asText = plan,
      node = PlanNode(title = "TODO", nodeType = "?")
    )
  }
}
