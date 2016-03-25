package models.query

import java.util.UUID

case class SavedQuery(
  id: UUID,
  owner: UUID,
  title: String,
  sql: String,
  lastRan: Option[Long] = None,
  created: Long,
  updated: Long
)
