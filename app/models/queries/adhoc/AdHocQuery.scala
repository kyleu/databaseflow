package models.queries.adhoc

import java.util.UUID

import org.joda.time.LocalDateTime

case class AdHocQuery(
  id: UUID,
  title: String,
  sql: String,
  created: LocalDateTime,
  updated: LocalDateTime
)
