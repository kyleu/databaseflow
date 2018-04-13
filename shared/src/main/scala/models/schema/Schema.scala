package models.schema

import java.util.UUID

import util.JsonSerializers._

object Schema {
  implicit val jsonEncoder: Encoder[Schema] = deriveEncoder
  implicit val jsonDecoder: Decoder[Schema] = deriveDecoder
}

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
    timezone: Double,
    enums: Seq[EnumType],
    tables: Seq[Table],
    views: Seq[View],
    procedures: Seq[Procedure],

    detailsLoadedAt: Option[Long] = None
) {
  def getTable(name: String) = tables.find(_.name.equalsIgnoreCase(name))
  def getView(name: String) = views.find(_.name.equalsIgnoreCase(name))
  def getProcedure(name: String) = procedures.find(_.name.equalsIgnoreCase(name))
}
