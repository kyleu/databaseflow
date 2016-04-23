package models.queries.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.database.{ Row, Statement }
import models.engine.DatabaseEngine
import models.queries.BaseQueries
import utils.EncryptUtils

object ConnectionSettingsQueries extends BaseQueries[ConnectionSettings] {
  override protected val tableName = "connections"
  override protected val columns = Seq("id", "name", "owner", "public", "description", "engine", "url", "username", "password")
  override protected val searchColumns = columns

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getAll(orderBy: String = "name") = GetAll(orderBy = orderBy)
  def getVisible(owner: Option[UUID], orderBy: String = "name") = GetAll(
    whereClause = owner match {
    case Some(x) => Some("public = true or owner = ?")
    case None => Some("public = true")
  },
    orderBy = orderBy,
    values = owner.toSeq
  )
  def getById(id: UUID) = GetById(Seq(id))
  val search = Search
  val removeById = RemoveById

  case class Update(cs: ConnectionSettings) extends Statement {
    override def sql = s"update $tableName set name = ?, owner = ?, public = ?, description = ?, engine = ?, url = ?, username = ?, password = ? where id = ?"
    override def values = Seq(cs.name, cs.owner, cs.public, cs.description, cs.engine.id, cs.url, cs.username, cs.password, cs.id)
  }

  override protected def fromRow(row: Row) = ConnectionSettings(
    id = row.as[UUID]("id"),
    name = row.as[String]("name"),
    owner = row.asOpt[UUID]("owner"),
    public = row.as[Boolean]("public"),
    description = row.as[String]("description"),
    engine = DatabaseEngine.get(row.as[String]("engine")),
    url = row.as[String]("url"),
    username = row.as[String]("username"),
    password = EncryptUtils.decrypt(row.as[String]("password"))
  )

  override protected def toDataSeq(q: ConnectionSettings) = {
    Seq[Any](q.id, q.name, q.owner, q.public, q.description, q.engine.toString, q.url, q.username, EncryptUtils.encrypt(q.password))
  }
}
