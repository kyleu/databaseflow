package models.result

import java.util.UUID

import models.query.QueryResult
import models.user.Role
import org.joda.time.LocalDateTime

case class SharedResult(
  id: UUID,
  title: String,
  owner: UUID,
  viewableBy: Role,
  connectionId: UUID,
  source: QueryResult.Source,
  lastAccessed: LocalDateTime,
  created: LocalDateTime
)
