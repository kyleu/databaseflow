package models.database

import java.util.UUID

import models.engine.DatabaseEngine
import services.database.ssl.SslSettings

case class PoolSettings(
  id: UUID = UUID.randomUUID,
  connectionId: UUID,
  name: Option[String] = None,
  engine: DatabaseEngine,
  url: String,
  username: String,
  password: String,
  maxWait: Long = 1000,
  maxSize: Int = 16,
  jdbcProperties: Map[String, String] = Map.empty,
  sslSettings: Option[SslSettings] = None,
  connectionInitSql: Option[String] = None
)
