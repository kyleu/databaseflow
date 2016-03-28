package models.queries.connection

import java.util.UUID

import models.database.{ Row, Statement }
import models.engine.{ ConnectionSettings, DatabaseEngine }
import models.queries.BaseQueries
import utils.EncryptUtils

object ConnectionQueries extends BaseQueries[ConnectionSettings] {
  override protected val tableName = "connections"
  override protected val columns = Seq("id", "name", "engine", "url", "username", "password")
  override protected val searchColumns = columns

  val insert = Insert
  def getAll(orderBy: String = "name") = GetAll(orderBy = orderBy)
  def getById(id: UUID) = GetById(Seq(id))
  val search = Search
  val removeById = RemoveById

  case class Update(cs: ConnectionSettings) extends Statement {
    override def sql = s"update $tableName set name = ?, engine = ?, url = ?, username = ?, password = ? where id = ?"
    override def values = Seq(cs.name, cs.engine.id, cs.url, cs.username, cs.password, cs.id)
  }

  override protected def fromRow(row: Row) = {
    val id = row.as[UUID]("id")
    val name = row.as[String]("name")
    val engine = DatabaseEngine.get(row.as[String]("engine"))
    val url = row.as[String]("url")
    val username = row.as[String]("username")
    val password = row.as[String]("password")
    val decryptedPassword = EncryptUtils.decrypt(password)

    ConnectionSettings(id, name, engine, url, username, decryptedPassword)
  }

  override protected def toDataSeq(q: ConnectionSettings) = {
    Seq[Any](q.id, q.name, q.engine.toString, q.url, q.username, EncryptUtils.encrypt(q.password))
  }
}
