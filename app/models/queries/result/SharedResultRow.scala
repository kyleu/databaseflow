package models.queries.result

import java.util.UUID

import models.database.Row
import models.query.{QueryFilter, QueryResult, SharedResult}
import models.schema.{ColumnType, FilterOp}
import models.user.Permission
import services.schema.JdbcHelper
import utils.JdbcUtils

object SharedResultRow {
  def fromRow(row: Row) = SharedResult(
    id = row.as[UUID]("id"),
    title = row.as[String]("title"),
    description = row.asOpt[Any]("description").map(s => JdbcUtils.extractString(s)),
    owner = row.as[UUID]("owner"),
    viewableBy = Permission.withName(row.as[String]("viewable_by")),
    connectionId = row.as[UUID]("connection_id"),
    sql = row.as[String]("sql"),
    source = QueryResult.Source(
      t = row.as[String]("source_type"),
      name = row.as[String]("source_name"),
      sortedColumn = row.asOpt[String]("source_sort_column"),
      sortedAscending = row.asOpt[Long]("source_sort_asc").map(_ != 0L),
      filters = row.asOpt[String]("filter_column") match {
        case Some(col) => Seq(QueryFilter(
          col = col,
          op = FilterOp.withName(row.as[String]("filter_op")),
          t = ColumnType.withName(row.as[String]("filter_type")),
          v = row.asOpt[String]("filter_value").getOrElse("")
        ))
        case None => Nil
      }
    ),
    chart = row.asOpt[Any]("chart").map(s => JdbcHelper.stringVal(s)),
    lastAccessed = row.as[java.sql.Timestamp]("last_accessed").getTime,
    created = row.as[java.sql.Timestamp]("created").getTime
  )
}
