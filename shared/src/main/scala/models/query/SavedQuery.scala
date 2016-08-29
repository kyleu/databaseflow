package models.query

import java.util.UUID

import models.user.Permission

case class SavedQuery(
  id: UUID = UUID.randomUUID,
  name: String = "Untitled Query",
  description: Option[String] = None,
  sql: String = "",

  owner: UUID,
  connection: Option[UUID] = None,
  read: Permission = Permission.Visitor,
  edit: Permission = Permission.Private,

  lastRan: Option[Long] = None,

  created: Long = System.currentTimeMillis,
  updated: Long = System.currentTimeMillis,
  loadedAt: Long = System.currentTimeMillis
)
