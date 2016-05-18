package models.result

import java.util.UUID

case class CachedResult(
  resultId: UUID,
  queryId: UUID,
  owner: Option[UUID],
  sql: String
)
