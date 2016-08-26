package models.query

import java.util.UUID

case class SharedResult(
  id: UUID,
  title: String,
  description: Option[String],
  owner: UUID,
  viewableBy: String,
  connectionId: UUID,
  source: QueryResult.Source,
  chart: Option[String],
  lastAccessed: Long = System.currentTimeMillis,
  created: Long = System.currentTimeMillis
)
