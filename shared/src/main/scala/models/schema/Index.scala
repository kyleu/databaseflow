package models.schema

import util.JsonSerializers._

object Index {
  implicit val jsonEncoder: Encoder[Index] = deriveEncoder
  implicit val jsonDecoder: Decoder[Index] = deriveDecoder
}

case class Index(
    name: String,
    unique: Boolean,
    indexType: String,
    cardinality: Long,
    columns: Seq[IndexColumn]
)
