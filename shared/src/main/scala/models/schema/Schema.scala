package models.schema

case class Schema(
  name: String,
  engine: String,
  engineVersion: String,
  driver: String,
  driverVersion: String,
  schemaTerm: String,
  tables: Seq[Table] = Nil,
  procedures: Seq[Procedure] = Nil
)
