package services.database

import java.io.FileInputStream
import java.security.KeyStore
import java.util.{Properties, UUID}

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import models.database.ConnectionSettings
import models.engine.EngineRegistry
import services.database.ssl.{ClientSideCertSslSocketFactoryFactory, SslParams, SslSettings}

object DatabaseService {
  def init() = {
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
        (k, v) <- initSsl(settings)
      } {
        addDataSourceProperty(k, v)
      }
    }

    val poolDataSource  = new HikariDataSource(poolConfig)

    new Database(poolDataSource, cs.metricRegistry)
  }

  protected def initSsl(ssl: SslSettings): Map[String, String] = {
    val clientCertKeyStoreProvider = ssl.clientCertKeyStoreProvider.getOrElse(KeyStore.getDefaultType)
    val clientCertKeyStorePassword = ssl.clientCertKeyStorePassword.map(_.toCharArray).orNull
    val clientCertKeyStoreStream = new FileInputStream(ssl.clientCertKeyStorePath)
    val clientCertKeyStore = KeyStore.getInstance(clientCertKeyStoreProvider)

    val trustKeyStoreProvider = ssl.trustKeyStoreProvider.getOrElse(KeyStore.getDefaultType)
    val trustKeyStoreStream = new FileInputStream(ssl.trustKeyStoreProviderPath)
    val trustKeyStore = KeyStore.getInstance(trustKeyStoreProvider)

    clientCertKeyStore.load(clientCertKeyStoreStream, clientCertKeyStorePassword)
    trustKeyStore.load(trustKeyStoreStream, null)

    val identifier = UUID.randomUUID().toString
    val sslParams = SslParams(clientCertKeyStore,
      ssl.clientCertKeyStorePassword.orNull,
      trustKeyStore)

    ClientSideCertSslSocketFactoryFactory.configure(identifier, sslParams)

    Map(
      "ssl" -> "true",
      "sslfactory" -> "com.simple.jdub.ClientSideCertSslSocketFactoryFactory",
      "sslfactoryarg" -> identifier
    )
  }
}
