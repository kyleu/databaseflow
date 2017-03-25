package services.database.core

import java.util.UUID

import models.connection.ConnectionSettings
import models.user.User
import services.config.{ConfigFileService, DatabaseConfig}
import services.database.{DatabaseConnection, DatabaseRegistry}
import utils.{Logging, PasswordEncryptUtils, SlugUtils}

import scala.util.control.NonFatal

abstract class CoreDatabase extends Logging {
  def connectionId: UUID
  def name: String
  def slug: String
  def title: String
  def description: String
  def configKey: String
  def dbName: String

  private[this] lazy val config = {
    val cfg = ConfigFileService.config.getConfig(s"databaseflow.$configKey")
    DatabaseConfig.fromConfig(cfg)
  }

  private[this] lazy val finalUrl = if (config.url.isEmpty || config.url == "default") {
    s"jdbc:h2:${ConfigFileService.configDir.getAbsolutePath}/$dbName"
  } else {
    config.url
  }

  private[this] var connOpt: Option[DatabaseConnection] = None

  var settings: Option[ConnectionSettings] = None

  def conn = connOpt.getOrElse(throw new IllegalStateException(s"$name database connection not open."))

  def isOpen = connOpt.isDefined

  def open() = {
    connOpt.foreach(x => throw new IllegalStateException(s"$name database connection already open."))

    settings = Some(ConnectionSettings(
      id = connectionId,
      name = title,
      slug = slug,
      owner = User.mock.id,
      description = description,
      urlOverride = Some(finalUrl),
      username = config.username,
      password = PasswordEncryptUtils.encrypt(config.password),
      engine = config.engine
    ))

    val database = try {
      DatabaseRegistry.db(User.mock, connectionId)
    } catch {
      case NonFatal(ex) =>
        val msg = s"Unable to connect to ${name.toLowerCase} database using engine [${config.engine}] with url [$finalUrl]."
        throw new IllegalStateException(msg, ex)
    }

    log.info(s"$name database started as user [${config.username}] against url [$finalUrl].")

    connOpt = Some(database)
  }

  def close() = {
    connOpt.foreach(_.close())
    connOpt = None
    log.info(s"$name database connection closed.")
  }
}
