package models.query

import java.util.UUID

import models.schema.{ColumnType, FilterOp}

object QueryResult {
  case class Col(
    name: String,
    t: ColumnType,
    precision: Option[Int] = None,
    scale: Option[Int] = None,
    primaryKey: Boolean = false,
    relationTable: Option[String] = None,
    relationColumn: Option[String] = None
  )

  case class Source(
      t: String,
      name: String,
      sortable: Boolean = true,
      sortedColumn: Option[String] = None,
      sortedAscending: Option[Boolean] = None,
      filterColumn: Option[String] = None,
      filterOp: Option[FilterOp] = None,
      filterType: Option[ColumnType] = None,
      filterValue: Option[String] = None,
      dataOffset: Int = 0
  ) {
    def asRowDataOptions(limit: Option[Int]) = RowDataOptions(
      orderByCol = sortedColumn,
      orderByAsc = sortedAscending,
      filterCol = filterColumn,
      filterOp = filterOp,
      filterType = filterType,
      filterVal = filterValue,
      limit = limit,
      offset = Some(dataOffset)
    )
  }
}

case class QueryResult(
  queryId: UUID,
  sql: String,
  isStatement: Boolean = false,
  columns: Seq[QueryResult.Col] = Nil,
  data: Seq[Seq[Option[String]]] = Nil,
  rowsAffected: Int = 0,
  moreRowsAvailable: Boolean = false,
  source: Option[QueryResult.Source] = None,
  elapsedMs: Int,
  occurred: Long = System.currentTimeMillis
)
