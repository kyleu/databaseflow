package models.plan

import java.util.UUID

case class QueryPlan(
  id: UUID,
  name: String,
  root: PlanNode,
  query: String,
  maxCost: Int,
  maxRows: Int,
  maxDuration: Int,
  created: Long
)
