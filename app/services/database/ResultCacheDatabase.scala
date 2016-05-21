package services.database

import java.util.UUID

import models.settings.SettingKey
import services.settings.SettingsService
import utils.Logging

object ResultCacheDatabase extends Logging {
  private[this] var connOpt: Option[DatabaseConnection] = None

  def open() = {
    connOpt.foreach(x => throw new IllegalStateException("Result Cache database already open."))

    val uuid = UUID.fromString(SettingsService(SettingKey.QueryCacheConnection))

    connOpt = MasterDatabase.databaseFor(uuid) match {
      case Left(x) => throw x
      case Right(db) => Some(db)
    }

    log.info(s"Result Cache database started as user [${conn.username}] against url [${conn.url}].")
  }

  def conn = connOpt.getOrElse(throw new IllegalStateException("Result Cache database connection not open."))

  def close() = {
    connOpt.foreach(_.close())
    connOpt = None
    log.info("Result Cache database closed.")
  }
}
