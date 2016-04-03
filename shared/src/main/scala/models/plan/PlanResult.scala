package models.plan

import java.util.UUID

case class PlanResult(
  queryId: UUID,
  name: String,
  action: String,
  sql: String,
  raw: String,
  node: PlanNode
)
