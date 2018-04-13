package models.schema

import util.JsonSerializers._

object PrimaryKey {
  implicit val jsonEncoder: Encoder[PrimaryKey] = deriveEncoder
  implicit val jsonDecoder: Decoder[PrimaryKey] = deriveDecoder
}

case class PrimaryKey(
    name: String,
    columns: List[String]
)
