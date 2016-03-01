package models.queries

import java.util.UUID

import org.joda.time.LocalDateTime

final case class AdHocQuery(
  id: UUID,
  title: String,
  sql: String,
  created: LocalDateTime,
  updated: LocalDateTime
)
