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
  procedureTerm: String,
  catalogTerm: String,
  maxSqlLength: Int,
  tables: Seq[String],
  views: Seq[String],
  procedures: Seq[String],
  clientInfoProperties: Seq[ClientInfoProperty]
)
