package models.query

object QueryResult {
  case class Col(name: String, t: String)
}

case class QueryResult(sql: String, columns: Seq[QueryResult.Col], data: Seq[Seq[Option[String]]])

