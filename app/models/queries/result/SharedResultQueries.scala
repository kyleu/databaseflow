package models.queries.result

import java.sql.Timestamp
import java.util.UUID

import models.database.Row
import models.queries.BaseQueries
import models.query.QueryResult
import models.result.SharedResult
import models.schema.FilterOp
import models.user.Role
import utils.{DateUtils, JdbcUtils}

object SharedResultQueries extends BaseQueries[SharedResult] {
  override protected val tableName = "shared_results"
  override protected val columns = Seq(
    "id", "title", "owner", "viewable_by", "connection_id",
    "source_type", "source_name", "source_sort_column", "source_sort_asc",
    "filter_column", "filter_op", "filter_value",
    "last_accessed", "created"
  )
  override protected val searchColumns = Seq("id", "title", "owner", "source_name", "filter_column", "filter_value")

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getById(id: UUID) = GetById(Seq(id))
  val getAll = GetAll
  val search = Search
  val removeById = RemoveById

  override protected def fromRow(row: Row) = SharedResult(
    id = row.as[UUID]("id"),
    title = row.as[String]("title"),
    owner = row.as[UUID]("owner"),
    viewableBy = Role.withName(row.as[String]("viewable_by")),
    connectionId = row.as[UUID]("connection_id"),
    source = QueryResult.Source(
      t = row.as[String]("source_type"),
      name = row.as[String]("source_name"),
      sortedColumn = row.asOpt[String]("source_sort_column"),
      sortedAscending = row.asOpt[Boolean]("source_sort_asc"),
      filterColumn = row.asOpt[String]("filter_column"),
      filterOp = row.asOpt[String]("filter_op").map(FilterOp.withName),
      filterValue = row.asOpt[String]("filter_value")
    ),
    lastAccessed = JdbcUtils.toLocalDateTime(row, "last_accessed"),
    created = JdbcUtils.toLocalDateTime(row, "created")
  )

  override protected def toDataSeq(sr: SharedResult) = Seq[Any](
    sr.id, sr.title, sr.owner, sr.viewableBy.toString, sr.connectionId, sr.source.t, sr.source.name, sr.source.sortedColumn, sr.source.sortedAscending,
    sr.source.filterColumn, sr.source.filterOp, sr.source.filterValue,
    new Timestamp(DateUtils.toMillis(sr.lastAccessed)), new Timestamp(DateUtils.toMillis(sr.created))
  )
}
