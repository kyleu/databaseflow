package models.queries.connection

import java.util.UUID

import models.database.{ Row, Statement }
import models.engine.{ ConnectionSettings, DatabaseEngine }
import models.queries.BaseQueries
import utils.EncryptUtils

object ConnectionSettingsQueries extends BaseQueries[ConnectionSettings] {
  override protected val tableName = "connections"
  override protected val columns = Seq("id", "name", "description", "engine", "url", "username", "password")
  override protected val searchColumns = columns

  val insert = Insert
  def delete(id: UUID) = Delete(Seq(id))
  def getAll(orderBy: String = "name") = GetAll(orderBy = orderBy)
  def getById(id: UUID) = GetById(Seq(id))
  val search = Search
  val removeById = RemoveById

  case class Update(cs: ConnectionSettings) extends Statement {
    override def sql = s"update $tableName set name = ?, description = ?, engine = ?, url = ?, username = ?, password = ? where id = ?"
    override def values = Seq(cs.name, cs.description, cs.engine.id, cs.url, cs.username, cs.password, cs.id)
  }

  override protected def fromRow(row: Row) = ConnectionSettings(
    id = row.as[UUID]("id"),
    name = row.as[String]("name"),
    description = row.as[String]("description"),
    engine = DatabaseEngine.get(row.as[String]("engine")),
    url = row.as[String]("url"),
    username = row.as[String]("username"),
    password = EncryptUtils.decrypt(row.as[String]("password"))
  )

  override protected def toDataSeq(q: ConnectionSettings) = {
    Seq[Any](q.id, q.name, q.description, q.engine.toString, q.url, q.username, EncryptUtils.encrypt(q.password))
  }
}
