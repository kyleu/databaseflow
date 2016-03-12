package models.schema

case class Schema(
  name: String,
  url: String,
  username: String,
  engine: String,
  engineVersion: String,
  driver: String,
  driverVersion: String,
  schemaTerm: String,
  maxSqlLength: Int,
  tables: Seq[Table] = Nil,
  procedures: Seq[Procedure] = Nil,
  functions: Seq[DatabaseFunction] = Nil
)
