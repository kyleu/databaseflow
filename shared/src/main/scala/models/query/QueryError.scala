package models.query

case class QueryError(sql: String, code: String, message: String, line: Option[Int] = None, position: Option[Int] = None)

