package models.query

import java.util.UUID

case class StatementResult(
  queryId: UUID,
  title: String,
  sql: String,
  rowsAffected: Int,
  occurred: Long = System.currentTimeMillis
)

