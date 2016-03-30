package models.queries.query

import java.util.UUID

import models.database.{ Row, Statement }
import models.queries.BaseQueries
import models.query.SavedQuery
import utils.DateUtils

object SavedQueryQueries extends BaseQueries[SavedQuery] {
  override protected val tableName = "saved_queries"
  override protected val columns = Seq("id", "title", "sql", "owner", "connection", "public", "last_ran", "created", "updated")
  override protected val searchColumns = Seq("id", "owner", "connection", "title", "sql")

  val insert = Insert
  def getById(id: UUID) = GetById(Seq(id))
  def getByOwner(userId: UUID) = GetAll(
    whereClause = Some("owner = ? or owner is null"),
    orderBy = "title",
    values = Seq(userId)
  )
  val search = Search
  val removeById = RemoveById

  case class UpdateSavedQuery(id: UUID, title: String, sqlString: String) extends Statement {
    override val sql = updateSql(Seq("title", "sql", "updated"))
    override val values = Seq[Any](title, sqlString, DateUtils.now, id)
  }

  override protected def fromRow(row: Row) = {
    SavedQuery(
      id = row.as[UUID]("id"),

      title = row.as[String]("title"),
      sql = row.as[String]("sql"),

      owner = row.asOpt[UUID]("owner"),
      connection = row.asOpt[UUID]("connection"),
      public = row.as[Boolean]("public"),

      lastRan = row.asOpt[java.sql.Timestamp]("last_ran").map(_.getTime),
      created = row.as[java.sql.Timestamp]("created").getTime,
      updated = row.as[java.sql.Timestamp]("updated").getTime
    )
  }

  override protected def toDataSeq(q: SavedQuery) = {
    Seq[Any](q.id, q.title, q.sql, q.owner, q.connection, q.public, q.lastRan, new java.sql.Timestamp(q.created), new java.sql.Timestamp(q.updated))
  }
}
