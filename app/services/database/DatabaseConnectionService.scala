package services.database

import java.util.Properties

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import models.database.PoolSettings
import models.engine.DatabaseEngine
import services.database.ssl.SslInit
import utils.metrics.{Checked, Instrumented}

object DatabaseConnectionService {
  private[this] var initialized = false

  def init() = if (!initialized) {
    initialized = true
    DatabaseEngine.values.foreach { r =>
      Class.forName(r.driverClass)
    }
  }

  def connect(cs: PoolSettings): DatabaseConnection = {
    if (!initialized) {
      init()
    }
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
      setMinimumIdle(1)
      setMaximumPoolSize(cs.maxSize)

      setHealthCheckRegistry(Checked.healthCheckRegistry)
      setMetricRegistry(Instrumented.metricRegistry)

      cs.connectionInitSql.foreach(setConnectionInitSql)

      for {
        settings <- cs.sslSettings
        (k, v) <- SslInit.initSsl(settings)
      } {
        addDataSourceProperty(k, v)
      }
    }

    val poolDataSource = new HikariDataSource(poolConfig)

    DatabaseConnection(cs.connectionId, cs.name.getOrElse(cs.id.toString), poolDataSource, cs.engine, cs.url, cs.username)
  }
}
