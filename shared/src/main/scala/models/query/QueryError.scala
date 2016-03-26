package models.query

import java.util.UUID

case class QueryError(
  queryId: UUID,
  sql: String,
  code: String,
  message: String,
  line: Option[Int] = None,
  position: Option[Int] = None
)

