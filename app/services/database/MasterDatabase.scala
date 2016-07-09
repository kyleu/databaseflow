package services.database

import java.util.UUID

import models.connection.ConnectionSettings
import models.database._
import services.data.MasterDdl
import services.config.{ConfigFileService, DatabaseConfig}
import utils.Logging

import scala.util.control.NonFatal

object MasterDatabase extends Logging {
  val connectionId = UUID.fromString("00000000-0000-0000-0000-000000000000")

  private[this] val config = {
    val cfg = ConfigFileService.config.getConfig("databaseflow.master")
    DatabaseConfig.fromConfig(cfg)
  }

  private[this] val finalUrl = if (config.url.isEmpty || config.url == "default") {
    s"jdbc:h2:${ConfigFileService.configDir.getAbsolutePath}/databaseflow"
  } else {
    config.url
  }

  private[this] var connOpt: Option[DatabaseConnection] = None

  var settings: Option[ConnectionSettings] = None

  def conn = connOpt.getOrElse(throw new IllegalStateException("Master database connection not open."))

  def isOpen = connOpt.isDefined
  def open() = {
    connOpt.foreach(x => throw new IllegalStateException("Master database connection already open."))

    settings = Some(ConnectionSettings(
      id = MasterDatabase.connectionId,
      engine = config.engine,
      name = s"${utils.Config.projectName} Storage",
      description = s"Internal storage used by ${utils.Config.projectName}.",
      url = finalUrl,
      username = config.username,
      password = config.password
    ))

    val database = try {
      DatabaseRegistry.db(MasterDatabase.connectionId)
    } catch {
      case NonFatal(ex) => throw new IllegalStateException(s"Unable to connect to master database using engine [${config.engine}] with url [$finalUrl].", ex)
    }

    log.info(s"Master database started as user [${config.username}] against url [$finalUrl].")

    MasterDdl.update(database)

    connOpt = Some(database)
  }

  def close() = {
    connOpt.foreach(_.close())
    connOpt = None
    log.info("Master database connection closed.")
  }

  def query[A](q: RawQuery[A]) = conn.query(q)
  def executeUnknown[A](q: Query[A], resultId: Option[UUID] = None) = conn.executeUnknown(q, resultId)
  def executeUpdate(s: Statement) = conn.executeUpdate(s)
  def transaction[A](f: Transaction => A) = conn.transaction(f)
}
