package models.schema

import util.JsonSerializers._

object Reference {
  implicit val jsonEncoder: Encoder[Reference] = deriveEncoder
  implicit val jsonDecoder: Decoder[Reference] = deriveDecoder
}

case class Reference(source: String, target: String)
