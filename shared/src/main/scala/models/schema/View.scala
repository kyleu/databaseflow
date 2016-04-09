package models.schema

case class View(
  name: String,
  catalog: Option[String],
  schema: Option[String],
  description: Option[String],
  definition: Option[String],

  columns: Seq[Column] = Nil,

  loadedAt: Long = System.currentTimeMillis
)
