package models.plan

import java.util.UUID

case class PlanResult(
  queryId: UUID,
  title: String,
  action: String,
  sql: String,
  raw: String,
  node: PlanNode,
  occurred: Long = System.currentTimeMillis
)
