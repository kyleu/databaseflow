package models.schema

case class Index(name: String, unique: Boolean, columns: Seq[IndexColumn])
