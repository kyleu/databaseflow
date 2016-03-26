package services.plan

import java.util.UUID

import models.plan.{ PlanNode, PlanResult }

object MySqlParseService extends PlanParseService("mysql") {
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
