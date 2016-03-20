package services.plan

import models.plan.{ PlanNode, PlanResult }

object PostgresParseService {
  def parse(sql: String, plan: String) = {
    PlanResult(
      name = "",
      action = "",
      sql = sql,
      asText = plan,
      node = PlanNode("TODO")
    )
  }
}
