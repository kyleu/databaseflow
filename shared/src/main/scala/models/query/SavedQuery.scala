package models.query

import java.util.UUID

case class SavedQuery(
  id: UUID,
  title: String = "Untitled Query",
  sql: String = "",

  owner: Option[UUID] = None,
  connection: Option[UUID] = None,
  public: Boolean = false,

  lastRan: Option[Long] = None,

  created: Long = System.currentTimeMillis,
  updated: Long = System.currentTimeMillis
)
