package models.query

import java.util.UUID

case class QueryError(
  queryId: UUID,
  title: String,
  sql: String,
  code: String,
  message: String,
  line: Option[Int] = None,
  position: Option[Int] = None,
  occurred: Long = System.currentTimeMillis
)
