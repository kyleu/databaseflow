package services.database

import java.util.UUID

import models.database.PoolSettings
import services.engine.ConnectionSettingsService
import utils.Logging

import scala.util.control.NonFatal

object MasterDatabase extends Logging {
  private[this] val databases = collection.mutable.HashMap.empty[UUID, Database]

  def databaseFor(connectionId: UUID) = databases.get(connectionId) match {
    case Some(c) => Right(c)
    case None =>
      val c = ConnectionSettingsService.getById(connectionId).getOrElse(throw new IllegalArgumentException(s"Unknown connection [$connectionId]."))
      val cs = PoolSettings(
        engine = c.engine,
        url = c.url,
        username = c.username,
        password = c.password
      )
      try {
        val ret = DatabaseService.connect(cs)
        databases(connectionId) = ret
        Right(ret)
      } catch {
        case NonFatal(x) => Left(x)
      }
  }

  private[this] var dbOpt: Option[Database] = None

  def open() = {
    dbOpt.foreach(x => throw new IllegalStateException("History database already open."))

    val database = databaseFor(ConnectionSettingsService.masterId) match {
      case Right(db) => db
      case Left(x) => throw x
    }

    log.info(s"Master database started as user [${ConnectionSettingsService.masterUsername}] against url [${ConnectionSettingsService.masterUrl}].")

    MasterDdl.update(database)

    dbOpt = Some(database)
  }

  def db = dbOpt.getOrElse(throw new IllegalStateException("Not open."))

  def close() = {
    databases.values.foreach(_.close())
    databases.clear()
    dbOpt.foreach(_.close())
    dbOpt = None
    log.info(s"Master database closed.")
  }
}
