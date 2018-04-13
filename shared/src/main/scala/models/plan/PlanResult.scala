package models.plan

import java.util.UUID

import util.JsonSerializers._

object PlanResult {
  implicit val jsonEncoder: Encoder[PlanResult] = deriveEncoder
  implicit val jsonDecoder: Decoder[PlanResult] = deriveDecoder
}

case class PlanResult(
    queryId: UUID,
    action: String,
    sql: String,
    raw: String,
    node: PlanNode,
    occurred: Long = System.currentTimeMillis
)
