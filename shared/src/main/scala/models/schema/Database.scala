package models.schema

case class Database(name: String, version: String, tables: Seq[Table])
