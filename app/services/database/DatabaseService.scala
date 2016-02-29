package services.database

import java.util.Properties

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import models.database.ConnectionSettings
import models.engine.EngineRegistry
import services.database.ssl.SslInit

object DatabaseService {
  private[this] var initialized = false

  def init() = if(initialized) {
    throw new IllegalStateException("Already initialized.")
  } else {
    EngineRegistry.all.foreach { r =>
      Class.forName(r.className)
    }
  }

  def connect(cs: ConnectionSettings): Database = {
    val properties = new Properties

    for { (k, v) <- cs.jdbcProperties } {
      properties.setProperty(k, v)
    }

    val poolConfig = new HikariConfig(properties) {
      setPoolName(cs.name.getOrElse(cs.url.replaceAll("[^A-Za-z0-9]", "")))
      setJdbcUrl(cs.url)
      setUsername(cs.username)
      setPassword(cs.password)
      setConnectionTimeout(cs.maxWait)
      setMaximumPoolSize(cs.maxSize)
      cs.healthCheckRegistry.foreach(setHealthCheckRegistry)
      cs.metricRegistry.foreach(setMetricRegistry)
      cs.connectionInitSql.foreach(setConnectionInitSql)

      for {
        settings <- cs.sslSettings
        (k, v) <- SslInit.initSsl(settings)
      } {
        addDataSourceProperty(k, v)
      }
    }

    val poolDataSource  = new HikariDataSource(poolConfig)

    new Database(poolDataSource, cs.metricRegistry)
  }
}
