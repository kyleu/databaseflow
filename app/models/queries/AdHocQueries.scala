package models.queries

import java.util.UUID

import models.database.{Query, Row, Statement}
import org.joda.time.LocalDateTime
import utils.DateUtils

object AdHocQueries extends BaseQueries[AdHocQuery] {
  override protected val tableName = "adhoc_queries"
  override protected val columns = Seq("id", "title", "sql", "created", "updated")
  override protected val searchColumns = Seq("id", "title", "author", "sql")

  val insert = Insert
  val getById = GetById
  val search = Search
  val removeById = RemoveById

  final case class UpdateAdHocQuery(id: UUID, title: String, sqlString: String) extends Statement {
    override val sql = updateSql(Seq("title", "sql", "updated"))
    override val values = Seq[Any](title, sqlString, DateUtils.now, id)
  }

  final case class AdHocQueryExecute(override val sql: String, override val values: Seq[Any]) extends Query[(Seq[String], Seq[Seq[Any]])] {
    override def reduce(rows: Iterator[Row]) = {
      val rowsList = rows.toList

      val columns = rowsList match {
        case Nil => Seq.empty[String]
        case head :: _ =>
          val md = head.rs.getMetaData
          (0 until md.getColumnCount).map { i =>
            md.getColumnName(i)
          }
      }

      val result = rowsList.map { r =>
        r.toMap.iterator.toSeq
      }.toSeq

      columns -> result
    }
  }

  override protected def fromRow(row: Row) = {
    val id = row.as[UUID]("id")
    val title = row.as[String]("title")
    val sql = row.as[String]("sql")
    val created = row.as[LocalDateTime]("created")
    val updated = row.as[LocalDateTime]("updated")

    AdHocQuery(id, title, sql, created, updated)
  }

  override protected def toDataSeq(q: AdHocQuery) = {
    Seq[Any](q.id, q.title, q.sql, q.created, q.updated)
  }
}
