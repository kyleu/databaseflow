package services.database

import java.util.UUID

import models.database.ConnectionSettings
import models.queries.connection.ConnectionQueries
import utils.Logging

object MasterDatabase extends Logging {
  def connectionFor(connectionId: UUID) = {
    val c = db.query(ConnectionQueries.getById(connectionId)).getOrElse(throw new IllegalArgumentException(s"Unknown connection [$connectionId]."))
    val cs = ConnectionSettings(
      url = c.url,
      username = c.username,
      password = c.password
    )
    DatabaseService.connect(cs)
  }

  private[this] var dbOpt: Option[Database] = None

  def open() = {
    dbOpt.foreach(x => throw new IllegalStateException("History database already open."))

    val cs = ConnectionSettings(
      url = "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified",
      username = "databaseflow",
      password = "flow",
      maxSize = 8
    )
    val database = DatabaseService.connect(cs)

    log.info(s"Master database started as user [${cs.username}] against url [${cs.url}].")

    MasterSchema.update(database)

    dbOpt = Some(database)
  }

  def db = dbOpt.getOrElse(throw new IllegalStateException("Not open."))

  def close() = {
    dbOpt.foreach(_.close())
    dbOpt = None
    log.info(s"Master database closed.")
  }
}
