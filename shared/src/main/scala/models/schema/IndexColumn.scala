package models.schema

import util.JsonSerializers._

object IndexColumn {
  implicit val jsonEncoder: Encoder[IndexColumn] = deriveEncoder
  implicit val jsonDecoder: Decoder[IndexColumn] = deriveDecoder
}

case class IndexColumn(name: String, ascending: Boolean) {
  override def toString = name + (if (ascending) { "" } else { " (desc)" })
}
