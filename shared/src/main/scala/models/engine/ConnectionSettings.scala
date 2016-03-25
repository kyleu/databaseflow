package models.engine

import java.util.UUID

import models.engine.rdbms.PostgreSQL

object ConnectionSettings {
  val defaultEngine = PostgreSQL
}

case class ConnectionSettings(
  id: UUID = UUID.randomUUID,
  name: String = "",
  engine: DatabaseEngine = ConnectionSettings.defaultEngine,
  url: String = ConnectionSettings.defaultEngine.exampleUrl,
  username: String = "",
  password: String = ""
)
