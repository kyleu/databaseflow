package models.result

import java.util.UUID

import org.joda.time.LocalDateTime

case class CachedResult(
  resultId: UUID,
  queryId: UUID,
  connectionId: UUID,
  owner: Option[UUID],
  status: String,
  sql: String,
  columns: Int,
  rows: Int,
  duration: Int,
  lastAccessed: LocalDateTime,
  created: LocalDateTime
)
