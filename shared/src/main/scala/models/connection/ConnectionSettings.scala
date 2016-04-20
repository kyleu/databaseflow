package models.connection

import java.util.UUID

import models.engine.DatabaseEngine
import models.engine.rdbms.PostgreSQL

object ConnectionSettings {
  val defaultEngine = PostgreSQL
  val empty = ConnectionSettings()
}

case class ConnectionSettings(
  id: UUID = UUID.randomUUID,
  name: String = "",
  owner: Option[UUID] = None,
  public: Boolean = true,
  description: String = "",
  engine: DatabaseEngine = ConnectionSettings.defaultEngine,
  url: String = ConnectionSettings.defaultEngine.exampleUrl,
  username: String = "",
  password: String = ""
)
