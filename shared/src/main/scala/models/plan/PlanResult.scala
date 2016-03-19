package models.plan

case class PlanResult(
  name: String,
  action: String,
  sql: String,
  asText: String,
  node: PlanNode
)
