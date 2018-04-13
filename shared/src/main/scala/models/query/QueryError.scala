package models.query

import java.util.UUID

import util.JsonSerializers._

object QueryError {
  implicit val jsonEncoder: Encoder[QueryError] = deriveEncoder
  implicit val jsonDecoder: Decoder[QueryError] = deriveDecoder
}

case class QueryError(
    queryId: UUID,
    sql: String,
    code: String,
    message: String,
    index: Option[Int] = None,
    elapsedMs: Int,
    occurred: Long = System.currentTimeMillis
)
