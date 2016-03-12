package models.schema

case class PrimaryKey(
  name: String,
  columns: Seq[String]
)
