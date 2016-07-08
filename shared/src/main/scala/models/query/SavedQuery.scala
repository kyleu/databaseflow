package models.query

import java.util.UUID

case class SavedQuery(
  id: UUID = UUID.randomUUID,
  name: String = "Untitled Query",
  description: Option[String] = None,
  sql: String = "",

  owner: Option[UUID] = None,
  connection: Option[UUID] = None,
  read: String = "visitor",
  edit: String = "private",

  lastRan: Option[Long] = None,

  created: Long = System.currentTimeMillis,
  updated: Long = System.currentTimeMillis,
  loadedAt: Long = System.currentTimeMillis
)
