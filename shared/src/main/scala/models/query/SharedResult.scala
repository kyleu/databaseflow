package models.query

import java.util.UUID

case class SharedResult(
  id: UUID,
  title: String,
  owner: UUID,
  viewableBy: String,
  connectionId: UUID,
  source: QueryResult.Source,
  lastAccessed: Long = System.currentTimeMillis,
  created: Long = System.currentTimeMillis
)
