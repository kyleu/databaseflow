package models.query

object QueryResult {
  case class Col(
    name: String,
    t: String,
    relationTable: Option[String] = None,
    relationColumn: Option[String] = None
  )
}

case class QueryResult(
  title: String,
  sql: String,
  columns: Seq[QueryResult.Col],
  data: Seq[Seq[Option[Any]]],
  occurred: Long
)

