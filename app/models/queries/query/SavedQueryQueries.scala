package models.queries.query

import java.util.UUID

import models.database.{ Row, Statement }
import models.queries.BaseQueries
import models.query.SavedQuery
import org.joda.time.LocalDateTime
import utils.DateUtils

object SavedQueryQueries extends BaseQueries[SavedQuery] {
  override protected val tableName = "saved_queries"
  override protected val columns = Seq("id", "owner", "title", "sql", "last_ran", "created", "updated")
  override protected val searchColumns = Seq("id", "owner", "title", "sql")

  val insert = Insert
  def getById(id: UUID) = GetById(Seq(id))
  def getByOwner(userId: UUID) = GetAll(whereClause = Some("owner = ?"), values = Seq(userId))
  val search = Search
  val removeById = RemoveById

  case class UpdateSavedQuery(id: UUID, title: String, sqlString: String) extends Statement {
    override val sql = updateSql(Seq("title", "sql", "updated"))
    override val values = Seq[Any](title, sqlString, DateUtils.now, id)
  }

  override protected def fromRow(row: Row) = {
    SavedQuery(
      id = row.as[UUID]("id"),
      owner = row.as[UUID]("owner"),
      title = row.as[String]("title"),
      sql = row.as[String]("sql"),
      lastRan = row.asOpt[LocalDateTime]("last_ran").map(DateUtils.toMillis),
      created = DateUtils.toMillis(row.as[LocalDateTime]("created")),
      updated = DateUtils.toMillis(row.as[LocalDateTime]("updated"))
    )
  }

  override protected def toDataSeq(q: SavedQuery) = {
    Seq[Any](q.id, q.owner, q.title, q.sql, q.lastRan, q.created, q.updated)
  }
}
