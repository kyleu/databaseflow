package models.schema

import java.util.UUID

import util.JsonSerializers._

object View {
  implicit val jsonEncoder: Encoder[View] = deriveEncoder
  implicit val jsonDecoder: Decoder[View] = deriveDecoder
}

case class View(
    name: String,
    connection: UUID,
    catalog: Option[String],
    schema: Option[String],
    description: Option[String],
    definition: Option[String],

    columns: Seq[Column] = Nil,

    loadedAt: Long = System.currentTimeMillis
)
