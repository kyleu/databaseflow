package services.database

import java.util.UUID

import models.connection.ConnectionSettings
import services.config.{ConfigFileService, DatabaseConfig}
import utils.Logging

import scala.util.control.NonFatal

object ResultCacheDatabase extends Logging {
  val connectionId = UUID.fromString("11111111-1111-1111-1111-111111111111")

  private[this] val config = {
    val cfg = ConfigFileService.config.getConfig("databaseflow.resultCache")
    DatabaseConfig.fromConfig(cfg)
  }

  private[this] val finalUrl = if (config.url.isEmpty || config.url == "default") {
    s"jdbc:h2:${ConfigFileService.configDir.getAbsolutePath}/result-cache"
  } else {
    config.url
  }

  private[this] var connOpt: Option[DatabaseConnection] = None

  var settings: Option[ConnectionSettings] = None

  def conn = connOpt.getOrElse(throw new IllegalStateException("Result cache database connection not open."))

  def isOpen = connOpt.isDefined
  def open() = {
    connOpt.foreach(x => throw new IllegalStateException("Result cache database connection already open."))

    settings = Some(ConnectionSettings(
      id = ResultCacheDatabase.connectionId,
      engine = config.engine,
      name = s"${utils.Config.projectName} Result Cache",
      description = s"Storage used by ${utils.Config.projectName} to cache query results.",
      url = finalUrl,
      username = config.username,
      password = config.password
    ))

    val database = try {
      DatabaseRegistry.db(ResultCacheDatabase.connectionId)
    } catch {
      case NonFatal(ex) =>
        val msg = s"Unable to connect to result cache database using engine [${config.engine}] with url [$finalUrl]."
        throw new IllegalStateException(msg, ex)
    }

    log.info(s"Result cache database started as user [${config.username}] against url [$finalUrl].")

    connOpt = Some(database)
  }

  def close() = {
    connOpt.foreach(_.close())
    connOpt = None
    log.info("Result cache database connection closed.")
  }
}
