package models.schema

import java.util.UUID

case class Schema(
  connectionId: UUID,
  schemaName: Option[String],
  catalog: Option[String],
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
  tables: Seq[Table],
  views: Seq[View],
  procedures: Seq[Procedure],

  detailsLoadedAt: Option[Long] = None
)
