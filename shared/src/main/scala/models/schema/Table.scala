package models.schema

case class Table(
  name: String,
  catalog: Option[String],
  schema: Option[String],
  description: Option[String],
  definition: Option[String],
  typeName: String,

  columns: Seq[Column] = Nil,
  rowIdentifier: Seq[String] = Nil,
  primaryKey: Option[PrimaryKey] = None,
  foreignKeys: Seq[ForeignKey] = Nil,
  indexes: Seq[Index] = Nil,

  loadedAt: Long = System.currentTimeMillis
)
