package models.schema

import util.JsonSerializers._

object Column {
  implicit val jsonEncoder: Encoder[Column] = deriveEncoder
  implicit val jsonDecoder: Decoder[Column] = deriveDecoder
}

case class Column(
    name: String,
    description: Option[String],
    definition: Option[String],
    primaryKey: Boolean,
    notNull: Boolean,
    autoIncrement: Boolean,
    columnType: ColumnType,
    sqlTypeCode: Int,
    sqlTypeName: String,
    size: String,
    sizeAsInt: Int,
    scale: Int,
    defaultValue: Option[String]
)
