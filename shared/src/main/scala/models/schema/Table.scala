package models.schema

case class Table(
  name: String,
  catalog: Option[String],
  schema: Option[String],
  description: Option[String],
  definition: Option[String],
  typeName: String,

  primaryKey: Option[PrimaryKey] = None,
  columns: Seq[Column] = Nil,
  foreignKeys: Seq[ForeignKey] = Nil,
  indices: Seq[Index] = Nil
)
