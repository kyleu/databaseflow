package models.queries.connection

import java.util.UUID

import models.connection.ConnectionSettings
import models.database.{Row, Statement}
import models.engine.DatabaseEngine
import models.queries.BaseQueries
import models.user.{Role, User}
import utils.PasswordEncryptUtils

object ConnectionSettingsQueries extends BaseQueries[ConnectionSettings] {
  override protected val tableName = "connections"
  override protected val columns = Seq("id", "name", "owner", "read", "edit", "description", "engine", "url", "username", "password")
  override protected val searchColumns = columns

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getAll(orderBy: String = "\"name\"") = GetAll(orderBy = orderBy)
  def getVisible(owner: Option[User], orderBy: String = "\"name\"") = {
    val readPerms = owner match {
      case Some(x) => if (x.role == Role.Admin) {
        "\"read\" in ('visitor', 'user', 'admin')"
      } else if (x.role == Role.User) {
        "\"read\" in ('visitor', 'user')"
      } else {
        "\"read\" = 'visitor'"
      }
      case None => "\"read\" = 'visitor'"
    }
    val ownerPerms = owner match {
      case Some(o) => " or \"owner\" = ?"
      case None => " or \"owner\" is null"
    }
    val values = owner match {
      case Some(o) => Seq(o.id)
      case None => Seq.empty
    }
    GetAll(
      whereClause = Some(readPerms + ownerPerms),
      orderBy = orderBy,
      values = values
    )
  }
  def getById(id: UUID) = GetById(Seq(id))
  val search = Search
  val removeById = RemoveById

  case class Update(cs: ConnectionSettings) extends Statement {
    override def sql = updateSql(Seq("name", "owner", "read", "edit", "description", "engine", "url", "username", "password"))
    override def values = Seq(cs.name, cs.owner, cs.read, cs.edit, cs.description, cs.engine.id, cs.url, cs.username, cs.password, cs.id)
  }

  override protected def fromRow(row: Row) = ConnectionSettings(
    id = row.as[UUID]("id"),
    name = row.as[String]("name"),
    owner = row.asOpt[UUID]("owner"),
    read = row.as[String]("read"),
    edit = row.as[String]("edit"),
    description = row.as[String]("description"),
    engine = DatabaseEngine.get(row.as[String]("engine")),
    url = row.as[String]("url"),
    username = row.as[String]("username"),
    password = PasswordEncryptUtils.decrypt(row.as[String]("password"))
  )

  override protected def toDataSeq(q: ConnectionSettings) = {
    Seq[Any](q.id, q.name, q.owner, q.read, q.edit, q.description, q.engine.toString, q.url, q.username, PasswordEncryptUtils.encrypt(q.password))
  }
}
