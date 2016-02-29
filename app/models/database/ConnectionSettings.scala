package models.database

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import services.database.ssl.SslSettings

case class ConnectionSettings(
  url: String,
  username: String,
  password: String,
  name: Option[String] = None,
  maxWait: Long = 1000,
  maxSize: Int = 8,
  jdbcProperties: Map[String, String] = Map.empty,
  sslSettings: Option[SslSettings] = None,
  healthCheckRegistry: Option[HealthCheckRegistry] = None,
  metricRegistry: Option[MetricRegistry] = None,
  connectionInitSql: Option[String] = None
)
