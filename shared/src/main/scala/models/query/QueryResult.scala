package models.query

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
  title: String,
  sql: String,
  columns: Seq[QueryResult.Col],
  data: Seq[Seq[Option[String]]],
  occurred: Long
)

