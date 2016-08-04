package models.queries.query

import java.util.UUID

import models.database.{Row, Statement}
import models.queries.BaseQueries
import models.query.SavedQuery
import utils.DateUtils

object SavedQueryQueries extends BaseQueries[SavedQuery] {
  override protected val tableName = "saved_queries"
  override protected val columns = Seq("id", "name", "description", "sql", "owner", "connection", "read", "edit", "last_ran", "created", "updated")
  override protected val searchColumns = Seq("id", "name", "description", "sql", "owner", "connection")

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getById(id: UUID) = GetById(Seq(id))

  def getVisible(userId: UUID) = {
    val ownerClause = """("owner" = ? or "owner" is null)"""
    val permClause = s"""(($ownerClause and "read" = 'private') or "read" != 'private')"""
    val values = Seq(userId)

    GetAll(
      whereClause = Some(permClause),
      orderBy = "\"name\"",
      values = values
    )
  }

  def getForUser(userId: UUID, connectionId: UUID) = {
    val ownerClause = """"owner" = ?"""
    val permClause = s"""(($ownerClause and "read" = 'private') or "read" != 'private')"""
    val values = Seq(userId, connectionId)

    GetAll(
      whereClause = Some(permClause + """ and ("connection" = ? or "connection" is null)"""),
      orderBy = "\"name\"",
      values = values
    )
  }
  val search = Search
  val removeById = RemoveById

  case class UpdateLastRan(id: UUID, lastRan: Option[Long]) extends Statement {
    override val sql = updateSql(Seq("last_ran"))
    override val values = Seq[Any](lastRan)
  }

  case class UpdateSavedQuery(sq: SavedQuery) extends Statement {
    override val sql = updateSql(Seq("name", "description", "sql", "owner", "connection", "read", "edit", "updated"))
    override val values = Seq[Any](sq.name, sq.description, sq.sql, sq.owner, sq.connection, sq.read, sq.edit, DateUtils.now, sq.id)
  }

  override protected def fromRow(row: Row) = {
    SavedQuery(
      id = row.as[UUID]("id"),

      name = row.as[String]("name"),
      description = row.asOpt[String]("description"),
      sql = row.as[String]("sql"),

      owner = row.as[UUID]("owner"),
      connection = row.asOpt[UUID]("connection"),
      read = row.as[String]("read"),
      edit = row.as[String]("edit"),

      lastRan = row.asOpt[java.sql.Timestamp]("last_ran").map(_.getTime),
      created = row.as[java.sql.Timestamp]("created").getTime,
      updated = row.as[java.sql.Timestamp]("updated").getTime
    )
  }

  override protected def toDataSeq(q: SavedQuery) = Seq[Any](
    q.id, q.name, q.description, q.sql, q.owner, q.connection, q.read, q.edit, q.lastRan, new java.sql.Timestamp(q.created), new java.sql.Timestamp(q.updated)
  )
}
