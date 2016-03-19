package models.database

import java.util.UUID

import models.engine.DatabaseEngine
import services.database.ssl.SslSettings

case class PoolSettings(
  id: UUID = UUID.randomUUID,
  engine: DatabaseEngine,
  url: String,
  username: String,
  password: String,
  name: Option[String] = None,
  maxWait: Long = 1000,
  maxSize: Int = 1,
  jdbcProperties: Map[String, String] = Map.empty,
  sslSettings: Option[SslSettings] = None,
  connectionInitSql: Option[String] = None
)
