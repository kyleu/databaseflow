package models.schema

case class Index(
  name: String,
  unique: Boolean,
  indexType: String,
  cardinality: Long,
  columns: Seq[IndexColumn]
)
