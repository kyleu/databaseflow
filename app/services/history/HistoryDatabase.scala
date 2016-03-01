package services.history

import models.database.ConnectionSettings
import services.database.{DatabaseService, Database}
import utils.Logging

object HistoryDatabase extends Logging {
  private[this] var dbOpt: Option[Database] = None

  def open() = {
    dbOpt.foreach(x => throw new IllegalStateException("History database already open."))

    val cs = ConnectionSettings(
      url = "jdbc:postgresql://localhost:5432/databaseflow",
      username = "databaseflow",
      password = "flow",
      maxSize = 8
    )
    val database = DatabaseService.connect(cs)

    log.info(s"History database started as user [${cs.username}] against url [${cs.url}].")

    dbOpt = Some(database)
  }

  def db = dbOpt.getOrElse(throw new IllegalStateException("Not open."))

  def close() = {
    dbOpt.foreach(_.close())
    dbOpt = None
    log.info(s"History database closed.")
  }
}
