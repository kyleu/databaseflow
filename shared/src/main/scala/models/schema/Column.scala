package models.schema

case class Column(
  name: String,
  description: Option[String],
  definition: Option[String],
  primaryKey: Boolean,
  notNull: Boolean,
  autoIncrement: Boolean,
  typeCode: Int,
  typeName: String,
  size: String,
  sizeAsInt: Int,
  scale: Int,
  defaultValue: Option[String]
)
