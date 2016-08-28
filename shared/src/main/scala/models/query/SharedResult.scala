package models.query

import java.util.UUID

case class SharedResult(
  id: UUID = UUID.randomUUID,
  title: String = "",
  description: Option[String] = None,
  owner: UUID,
  viewableBy: String = "user",
  connectionId: UUID,
  source: QueryResult.Source,
  chart: Option[String] = None,
  lastAccessed: Long = System.currentTimeMillis,
  created: Long = System.currentTimeMillis
)
