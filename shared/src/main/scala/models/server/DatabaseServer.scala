package models.server

import java.util.UUID

import models.engine.DatabaseEngine

case class DatabaseServer(
  id: UUID,
  name: String,
  engine: DatabaseEngine,
  hostname: String,
  port: Option[Int] = None,
  additionalParams: Option[Map[String, String]] = None
)
