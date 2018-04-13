package models.query

import util.JsonSerializers._

object QueryCheckResult {
  implicit val jsonEncoder: Encoder[QueryCheckResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[QueryCheckResult] = deriveDecoder
}

case class QueryCheckResult(
    sql: String,
    error: Option[String] = None,
    index: Option[Int] = None
)
