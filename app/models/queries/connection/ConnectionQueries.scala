package models.queries.connection

import java.util.UUID

import models.database.Row
import models.engine.DatabaseEngine
import models.queries.BaseQueries
import models.flow.Connection

object ConnectionQueries extends BaseQueries[Connection] {
  override protected val tableName = "connections"
  override protected val columns = Seq("id", "name", "engine", "url")
  override protected val searchColumns = columns

  val insert = Insert
  def getAll(orderBy: String = "name") = GetAll(orderBy)
  val getById = GetById
  val search = Search
  val removeById = RemoveById

  override protected def fromRow(row: Row) = {
    val id = row.as[UUID]("id")
    val name = row.as[String]("name")
    val engine = DatabaseEngine.get(row.as[String]("engine"))
    val url = row.as[String]("url")

    Connection(id, name, engine, url)
  }

  override protected def toDataSeq(q: Connection) = {
    Seq[Any](q.id, q.name, q.engine.toString, q.url)
  }
}
