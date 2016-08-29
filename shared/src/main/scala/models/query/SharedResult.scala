package models.query

import java.util.UUID

import models.user.Permission

case class SharedResult(
  id: UUID = UUID.randomUUID,
  title: String = "",
  description: Option[String] = None,
  owner: UUID,
  viewableBy: Permission = Permission.User,
  connectionId: UUID,
  source: QueryResult.Source,
  chart: Option[String] = None,
  lastAccessed: Long = System.currentTimeMillis,
  created: Long = System.currentTimeMillis
)
