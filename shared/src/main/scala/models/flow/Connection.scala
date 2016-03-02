package models.flow

import java.util.UUID

import models.engine.DatabaseEngine

case class Connection(
  id: UUID,
  name: String,
  engine: DatabaseEngine,
  url: String
)
