package models.query

import models.schema.{ColumnType, FilterOp}
import util.JsonSerializers._

object QueryFilter {
  implicit val jsonEncoder: Encoder[QueryFilter] = deriveEncoder
  implicit val jsonDecoder: Decoder[QueryFilter] = deriveDecoder
}

case class QueryFilter(col: String, op: FilterOp, t: ColumnType, v: String)
