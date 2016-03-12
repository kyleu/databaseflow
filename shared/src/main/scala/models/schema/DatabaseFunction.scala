package models.schema

case class DatabaseFunction(
  name: String,
  description: Option[String],
  params: Seq[String] = Nil
)
