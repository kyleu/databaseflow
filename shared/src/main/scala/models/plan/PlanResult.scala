package models.plan

import java.util.UUID

case class PlanResult(
  queryId: UUID,
  name: String,
  action: String,
  sql: String,
  asText: String,
  node: PlanNode
)
