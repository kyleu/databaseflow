package services.database

import java.util.UUID

import models.database.PoolSettings
import models.engine.rdbms._
import services.connection.ConnectionSettingsService
import services.data.MasterDdl
import utils.Logging

import scala.util.control.NonFatal

object MasterDatabase extends Logging {
  private[this] val databases = collection.mutable.HashMap.empty[UUID, DatabaseConnection]

  val connectionId = UUID.fromString("00000000-0000-0000-0000-000000000000")
  //val (engine, url) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  val (engine, url) = H2 -> "jdbc:h2:./tmp/databaseflow-master"

  val username = "databaseflow"
  val password = "flow"

  def databaseFor(connectionId: UUID) = databases.get(connectionId) match {
    case Some(c) => Right(c)
    case None =>
      val c = ConnectionSettingsService.getById(connectionId).getOrElse(throw new IllegalArgumentException(s"Unknown connection [$connectionId]."))
      val cs = PoolSettings(
        connectionId = connectionId,
        name = Some(c.id.toString),
        engine = c.engine,
        url = c.url,
        username = c.username,
        password = c.password
      )
      try {
        val ret = DatabaseConnectionService.connect(cs)
        databases(connectionId) = ret
        Right(ret)
      } catch {
        case NonFatal(x) => Left(x)
      }
  }

  def db(connectionId: UUID) = databaseFor(connectionId) match {
    case Right(x) => x
    case Left(x) => throw x
  }

  private[this] var connOpt: Option[DatabaseConnection] = None

  def open() = {
    connOpt.foreach(x => throw new IllegalStateException("History database already open."))

    val database = db(MasterDatabase.connectionId)

    log.info(s"Master database started as user [$username] against url [$url].")

    MasterDdl.update(database)

    connOpt = Some(database)
  }

  def conn = connOpt.getOrElse(throw new IllegalStateException("Master database connection not open."))

  def close() = {
    databases.values.foreach(_.close())
    databases.clear()
    connOpt.foreach(_.close())
    connOpt = None
    log.info("Master database closed.")
  }
}
