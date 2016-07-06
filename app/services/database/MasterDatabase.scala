package services.database

import java.util.UUID

import models.connection.ConnectionSettings
import models.database.PoolSettings
import models.engine.rdbms._
import models.user.User
import services.connection.ConnectionSettingsService
import services.data.MasterDdl
import utils.Logging

import scala.util.control.NonFatal

object MasterDatabase extends Logging {
  private[this] val databases = collection.mutable.HashMap.empty[UUID, (DatabaseConnection, ConnectionSettings)]

  val connectionId = UUID.fromString("00000000-0000-0000-0000-000000000000")
  val (engine, url) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  //val (engine, url) = H2 -> "jdbc:h2:./tmp/databaseflow-master"

  val username = "databaseflow"
  val password = "flow"

  private[this] def resultFor(c: (Boolean, String), db: DatabaseConnection) = if (c._1) {
    Right(db)
  } else {
    Left(new IllegalAccessError("Not authorized to view this connection. " + c._2))
  }

  def databaseFor(user: Option[User], connectionId: UUID) = databases.get(connectionId) match {
    case Some(c) => resultFor(ConnectionSettingsService.canRead(user, c._2), c._1)
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
        databases(connectionId) = ret -> c
        resultFor(ConnectionSettingsService.canRead(user, c), ret)
      } catch {
        case NonFatal(x) => Left(x)
      }
  }

  def db(connectionId: UUID) = databaseFor(None, connectionId) match {
    case Right(x) => x
    case Left(x) => throw x
  }

  private[this] var connOpt: Option[DatabaseConnection] = None
  def isOpen = connOpt.isDefined

  def open() = {
    connOpt.foreach(x => throw new IllegalStateException("History database already open."))

    val database = db(MasterDatabase.connectionId)

    log.info(s"Master database started as user [$username] against url [$url].")

    MasterDdl.update(database)

    connOpt = Some(database)
  }

  def conn = connOpt.getOrElse(throw new IllegalStateException("Master database connection not open."))

  def close() = {
    databases.values.foreach(_._1.close())
    databases.clear()
    connOpt.foreach(_.close())
    connOpt = None
    log.info("Master database closed.")
  }
}
