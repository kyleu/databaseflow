package models.queries.result

import java.util.UUID

import models.database.{Query, Row, Statement}
import models.queries.BaseQueries
import models.query.{QueryResult, SharedResult}
import models.schema.FilterOp
import models.user.Permission
import services.schema.JdbcHelper
import utils.JdbcUtils

object SharedResultQueries extends BaseQueries[SharedResult] {
  override protected val tableName = "shared_results"
  override protected val columns = Seq(
    "id", "title", "description", "owner", "viewable_by", "connection_id", "source_type", "source_name", "source_sort_column", "source_sort_asc",
    "filter_column", "filter_op", "filter_value", "chart", "last_accessed", "created"
  )
  override protected val searchColumns = Seq("id", "title", "owner", "source_name", "filter_column", "filter_value")

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getById(id: UUID) = GetById(Seq(id))
  val getAll = GetAll
  val search = Search
  val removeById = RemoveById

  case class UpdateSharedResult(sr: SharedResult) extends Statement {
    override val sql = updateSql(Seq(
      "title", "owner", "viewable_by", "connection_id", "source_type", "source_name", "source_sort_column", "source_sort_asc",
      "filter_column", "filter_op", "filter_value", "chart", "last_accessed", "created"
    ))
    override val values = Seq[Any](
      sr.title, sr.owner, sr.viewableBy.toString, sr.connectionId, sr.source.t, sr.source.name, sr.source.sortedColumn, sr.source.sortedAscending,
      sr.source.filterColumn, sr.source.filterOp, sr.source.filterValue, sr.chart,
      new java.sql.Timestamp(sr.lastAccessed), new java.sql.Timestamp(sr.created), sr.id
    )
  }

  case class UpdateLastAccessed(sr: UUID) extends Statement {
    override val sql = updateSql(Seq("last_accessed"))
    override val values = Seq[Any](new java.sql.Timestamp(System.currentTimeMillis), sr)
  }

  def getVisible(userId: UUID) = GetAll(
    whereClause = Some("""(("owner" = ? and "viewable_by" = 'private') or "viewable_by" != 'private')"""),
    orderBy = "\"title\"",
    values = Seq(userId)
  )

  def getForUser(userId: UUID, connectionId: UUID) = GetAll(
    whereClause = Some("""(("owner" = ? and "viewable_by" = 'private') or "viewable_by" != 'private') and ("connection_id" = ? or "connection_id" is null)"""),
    orderBy = "\"title\"",
    values = Seq(userId, connectionId)
  )

  case object GetCachedTableNames extends Query[Set[String]] {
    override val sql = s"""select "source_name" from "$tableName" where "source_type" = 'cache'"""
    override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("source_name")).toSet
  }

  case class ContainsCachedTableName(name: String) extends Query[Boolean] {
    override val sql = s"""select count(*) as c from "$tableName" where "source_name" = ?"""
    override def values = Seq(name)
    override def reduce(rows: Iterator[Row]) = rows.next().as[Long]("c") != 0L
  }

  override protected def fromRow(row: Row) = SharedResult(
    id = row.as[UUID]("id"),
    title = row.as[String]("title"),
    description = row.asOpt[Any]("description").map(s => JdbcUtils.extractString(s)),
    owner = row.as[UUID]("owner"),
    viewableBy = Permission.withName(row.as[String]("viewable_by")),
    connectionId = row.as[UUID]("connection_id"),
    source = QueryResult.Source(
      t = row.as[String]("source_type"),
      name = row.as[String]("source_name"),
      sortedColumn = row.asOpt[String]("source_sort_column"),
      sortedAscending = row.asOpt[Long]("source_sort_asc").map(_ != 0L),
      filterColumn = row.asOpt[String]("filter_column"),
      filterOp = row.asOpt[String]("filter_op").map(FilterOp.withName),
      filterValue = row.asOpt[String]("filter_value")
    ),
    chart = row.asOpt[Any]("chart").map(s => JdbcHelper.stringVal(s)),
    lastAccessed = row.as[java.sql.Timestamp]("last_accessed").getTime,
    created = row.as[java.sql.Timestamp]("created").getTime
  )

  override protected def toDataSeq(sr: SharedResult) = Seq[Any](
    sr.id, sr.title, sr.description, sr.owner, sr.viewableBy.toString, sr.connectionId,
    sr.source.t, sr.source.name, sr.source.sortedColumn, sr.source.sortedAscending,
    sr.source.filterColumn, sr.source.filterOp, sr.source.filterValue, sr.chart,
    new java.sql.Timestamp(sr.lastAccessed), new java.sql.Timestamp(sr.created)
  )
}
