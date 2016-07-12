package services.database

import java.util.UUID

import models.connection.ConnectionSettings
import models.database.PoolSettings
import models.user.User
import services.connection.ConnectionSettingsService
import services.database.core.MasterDatabase
import utils.Logging

import scala.util.control.NonFatal

object DatabaseRegistry extends Logging {
  private[this] val databases = collection.mutable.HashMap.empty[UUID, (DatabaseConnection, ConnectionSettings)]

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

  def flush(connectionId: UUID) = databases.remove(connectionId).foreach(_._1.close())

  def close() = {
    databases.values.foreach(_._1.close())
    databases.clear()
  }
}
