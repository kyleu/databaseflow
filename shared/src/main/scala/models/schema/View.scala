package models.schema

import java.util.UUID

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
