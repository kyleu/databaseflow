package models.schema

case class Table(
  name: String,
  catalog: Option[String],
  schema: String,
  description: Option[String],
  typeName: String,

  columns: Seq[Column],
  foreignKeys: Seq[ForeignKey],
  indices: Seq[Index]
)
