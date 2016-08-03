package models.query

case class QueryCheckResult(
  sql: String,
  error: Option[String] = None,
  index: Option[Int] = None
)
