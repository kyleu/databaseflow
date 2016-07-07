package services.database

import java.util.UUID

import models.connection.ConnectionSettings
import models.database._
import models.engine.rdbms._
import services.data.MasterDdl
import utils.Logging

import scala.util.control.NonFatal

object MasterDatabaseConnection extends Logging {
  private[this] val (engine, url) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  private[this] val (fallbackEngine, fallbackUrl) = H2 -> "jdbc:h2:./tmp/databaseflow-master"

  private[this] val username = "databaseflow"
  private[this] val password = "flow"

  private[this] var connOpt: Option[DatabaseConnection] = None

  var settings: Option[ConnectionSettings] = None

  def conn = connOpt.getOrElse(throw new IllegalStateException("Master database connection not open."))

  def isOpen = connOpt.isDefined
  def open() = {
    connOpt.foreach(x => throw new IllegalStateException("History database already open."))

    settings = Some(ConnectionSettings(
      id = MasterDatabase.connectionId,
      engine = engine,
      name = s"${utils.Config.projectName} Storage",
      description = s"Internal storage used by ${utils.Config.projectName}.",
      url = url,
      username = username,
      password = password
    ))

    val database = try {
      val ret = MasterDatabase.db(MasterDatabase.connectionId)
      log.info("Using PostgreSQL for caches and configuration.")
      ret
    } catch {
      case NonFatal(origEx) =>
        val path = "/opt/databaseflow" // TODO
        log.info(s"Using a local H2 database located in [$path].")
        log.info("To use PostgreSQL, configure PostgreSQL port 5432 to listen on [localhost], username [databaseflow], password [flow].")
        settings = settings.map(cs => cs.copy(engine = fallbackEngine, url = fallbackUrl))
        try {
          MasterDatabase.db(MasterDatabase.connectionId)
        } catch {
          case NonFatal(ex) => throw new IllegalStateException(ex.getClass.getSimpleName + ": " + ex.getMessage, origEx)
        }
    }

    log.info(s"Master database started as user [$username] against url [${settings.map(_.url).getOrElse("?")}].")

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
