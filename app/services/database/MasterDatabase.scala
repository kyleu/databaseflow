package services.database

import java.util.UUID

import models.database.PoolSettings
import models.engine.rdbms._
import models.queries.connection.ConnectionQueries
import utils.Logging

object MasterDatabase extends Logging {
  val (engine, url) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  //val (engine, url) = H2 -> "jdbc:h2:./db/databaseflow"

  private[this] val databases = collection.mutable.HashMap.empty[UUID, Database]

  def databaseFor(connectionId: UUID) = databases.getOrElseUpdate(connectionId, {
    val c = db.query(ConnectionQueries.getById(connectionId)).getOrElse(throw new IllegalArgumentException(s"Unknown connection [$connectionId]."))
    val cs = PoolSettings(
      url = c.url,
      username = c.username,
      password = c.password
    )
    DatabaseService.connect(cs)
  })

  private[this] var dbOpt: Option[Database] = None

  def open() = {
    dbOpt.foreach(x => throw new IllegalStateException("History database already open."))

    val cs = PoolSettings(
      url = url,
      username = "databaseflow",
      password = "flow",
      maxSize = 8
    )
    val database = DatabaseService.connect(cs)

    log.info(s"Master database started as user [${cs.username}] against url [${cs.url}].")

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
