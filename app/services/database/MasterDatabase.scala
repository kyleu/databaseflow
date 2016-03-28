package services.database

import java.util.UUID

import models.database.PoolSettings
import models.engine.rdbms._
import models.queries.connection.ConnectionQueries
import utils.Logging

import scala.util.control.NonFatal

object MasterDatabase extends Logging {
  val (engine, url) = PostgreSQL -> "jdbc:postgresql://localhost:5432/databaseflow?stringtype=unspecified"
  //val (engine, url) = H2 -> "jdbc:h2:./db/databaseflow"

  private[this] val databases = collection.mutable.HashMap.empty[UUID, Database]

  def databaseFor(connectionId: UUID) = databases.get(connectionId) match {
    case Some(c) => Right(c)
    case None =>
      val c = db.query(ConnectionQueries.getById(connectionId)).getOrElse(throw new IllegalArgumentException(s"Unknown connection [$connectionId]."))
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

    val cs = PoolSettings(
      engine = engine,
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
