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
  override protected val columns = Seq(
    "id", "name", "owner", "read", "edit", "description", "engine", "host", "db_name", "extra", "url_override", "username", "password"
  )
  override protected val searchColumns = columns

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getAll(orderBy: String = "\"name\"") = GetAll(orderBy = orderBy)
  def getVisible(owner: User, orderBy: String = "\"name\"") = {
    val readPerms = if (owner.role == Role.Admin) {
      "\"read\" in ('visitor', 'user', 'admin')"
    } else if (owner.role == Role.User) {
      "\"read\" in ('visitor', 'user')"
    } else {
      "\"read\" = 'visitor'"
    }
    val ownerPerms = " or \"owner\" = ?"
    val values = Seq(owner.id)
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
    override def sql = updateSql(Seq(
      "name", "owner", "read", "edit", "description", "engine", "host", "db_name", "extra", "url_override", "username", "password"
    ))
    override def values = Seq(
      cs.name, cs.owner, cs.read, cs.edit, cs.description, cs.engine.id, cs.host, cs.dbName, cs.extra, cs.urlOverride, cs.username, cs.password, cs.id
    )
  }

  override protected def fromRow(row: Row) = ConnectionSettings(
    id = row.as[UUID]("id"),
    name = row.as[String]("name"),
    owner = row.as[UUID]("owner"),
    read = row.as[String]("read"),
    edit = row.as[String]("edit"),
    description = row.as[String]("description"),
    engine = DatabaseEngine.withName(row.as[String]("engine")),
    host = row.asOpt[String]("host"),
    dbName = row.asOpt[String]("db_name"),
    extra = row.asOpt[String]("extra"),
    urlOverride = row.asOpt[String]("url_override"),
    username = row.as[String]("username"),
    password = PasswordEncryptUtils.decrypt(row.as[String]("password"))
  )

  override protected def toDataSeq(q: ConnectionSettings) = Seq[Any](
    q.id, q.name, q.owner, q.read, q.edit, q.description, q.engine.toString,
    q.host, q.dbName, q.extra, q.urlOverride, q.username, PasswordEncryptUtils.encrypt(q.password)
  )
}
