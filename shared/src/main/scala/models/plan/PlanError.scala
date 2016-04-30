package models.plan

import java.util.UUID

case class PlanError(
  queryId: UUID,
  title: String,
  sql: String,
  code: String,
  message: String,
  raw: Option[String] = None,
  occurred: Long = System.currentTimeMillis
)
