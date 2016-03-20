package services.plan

import models.plan.{ PlanNode, PlanResult }

object PostgresParseService extends PlanParseService("postgres") {
  override def parse(sql: String, plan: String) = {
    val json = upickle.json.read(plan)

    PlanResult(
      name = "",
      action = "",
      sql = sql,
      asText = plan,
      node = PlanNode("TODO")
    )
  }
}
