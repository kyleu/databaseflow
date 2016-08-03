package models.query

import java.util.UUID

case class QueryError(
  queryId: UUID,
  sql: String,
  code: String,
  message: String,
  index: Option[Int] = None,
  occurred: Long = System.currentTimeMillis
)
