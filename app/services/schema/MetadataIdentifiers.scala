package services.schema

import java.sql.DatabaseMetaData

import models.database.Row

object MetadataIdentifiers {
  def getRowIdentifier(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], name: String) = {
    val rs = metadata.getBestRowIdentifier(catalog.orNull, schema.orNull, name, DatabaseMetaData.bestRowSession, true)
    new Row.Iter(rs).map(_.as[String]("COLUMN_NAME")).toList
  }
}
