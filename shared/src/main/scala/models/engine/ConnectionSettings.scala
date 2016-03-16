package models.engine

import java.util.UUID

case class ConnectionSettings(
  id: UUID,
  name: String,
  engine: DatabaseEngine,
  url: String,
  username: String,
  password: String
)
