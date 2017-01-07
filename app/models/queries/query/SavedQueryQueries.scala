package models.queries.query

import java.util.UUID

import models.database.{Row, Statement}
import models.queries.BaseQueries
import models.query.SavedQuery
import models.user.Permission
import utils.{DateUtils, JdbcUtils}
import upickle.default._

import scala.util.control.NonFatal

object SavedQueryQueries extends BaseQueries[SavedQuery] {
  override protected val tableName = "saved_queries"
  override protected val columns = Seq("id", "name", "description", "sql", "params", "owner", "connection", "read", "edit", "last_ran", "created", "updated")
  override protected val searchColumns = Seq("id", "name", "description", "sql", "params", "owner", "connection")

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getById(id: UUID) = GetById(Seq(id))

  def getVisible(userId: UUID) = GetAll(
    whereClause = Some("""((("owner" = ? or "owner" is null) and "read" = 'private') or "read" != 'private')"""),
    orderBy = "\"name\"",
    values = Seq(userId)
  )

  def getForUser(userId: UUID, connectionId: UUID) = GetAll(
    whereClause = Some("""(("owner" = ? and "read" = 'private') or "read" != 'private') and ("connection" = ? or "connection" is null)"""),
    orderBy = "\"name\"",
    values = Seq(userId, connectionId)
  )

  val search = Search
  val removeById = RemoveById

  case class UpdateLastRan(id: UUID, lastRan: Option[Long]) extends Statement {
    override val sql = updateSql(Seq("last_ran"))
    override val values = Seq[Any](lastRan)
  }

  case class UpdateSavedQuery(sq: SavedQuery) extends Statement {
    override val sql = updateSql(Seq("name", "description", "sql", "params", "owner", "connection", "read", "edit", "updated"))
    override val values = Seq[Any](
      sq.name, sq.description, sq.sql, write(sq.params), sq.owner, sq.connection, sq.read.toString, sq.edit.toString, DateUtils.now, sq.id
    )
  }

  override protected def fromRow(row: Row) = SavedQuery(
    id = row.as[UUID]("id"),

    name = row.as[String]("name"),
    description = row.asOpt[Any]("description").map(JdbcUtils.extractString),
    sql = JdbcUtils.extractString(row.as[Any]("sql")),
    params = row.asOpt[Any]("params").map(JdbcUtils.extractString).map(x => try {
      read[Seq[SavedQuery.Param]](x)
    } catch {
      case NonFatal(_) => Seq.empty
    }).getOrElse(Seq.empty),

    owner = row.as[UUID]("owner"),
    connection = row.asOpt[UUID]("connection"),
    read = Permission.withName(row.as[String]("read")),
    edit = Permission.withName(row.as[String]("edit")),

    lastRan = row.asOpt[java.sql.Timestamp]("last_ran").map(_.getTime),
    created = row.as[java.sql.Timestamp]("created").getTime,
    updated = row.as[java.sql.Timestamp]("updated").getTime
  )

  override protected def toDataSeq(q: SavedQuery) = Seq[Any](
    q.id, q.name, q.description, q.sql, write(q.params), q.owner, q.connection, q.read.toString, q.edit.toString,
    q.lastRan, new java.sql.Timestamp(q.created), new java.sql.Timestamp(q.updated)
  )
}
