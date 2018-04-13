package models.plan

import java.util.UUID

import util.JsonSerializers._

object PlanError {
  implicit val jsonEncoder: Encoder[PlanError] = deriveEncoder
  implicit val jsonDecoder: Decoder[PlanError] = deriveDecoder
}

case class PlanError(
    queryId: UUID,
    sql: String,
    code: String,
    message: String,
    raw: Option[String] = None,
    occurred: Long = System.currentTimeMillis
)
