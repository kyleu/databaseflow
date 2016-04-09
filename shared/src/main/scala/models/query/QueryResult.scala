package models.query

import java.util.UUID

import models.schema.ColumnType

object QueryResult {
  case class Col(
    name: String,
    t: ColumnType,
    relationTable: Option[String] = None,
    relationColumn: Option[String] = None
  )
}

case class QueryResult(
  queryId: UUID,
  title: String,
  sql: String,
  columns: Seq[QueryResult.Col],
  data: Seq[Seq[Option[String]]],
  sortable: Boolean,
  sortedColumn: Option[String] = None,
  sortedAscending: Option[Boolean] = None,
  occurred: Long = System.currentTimeMillis
)

