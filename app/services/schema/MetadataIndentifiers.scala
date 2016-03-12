package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.Table

object MetadataIndentifiers {
  def getRowIdentifier(metadata: DatabaseMetaData, t: Table) = {
    val rs = metadata.getBestRowIdentifier(t.catalog.orNull, t.schema.orNull, t.name, DatabaseMetaData.bestRowSession, true)
    new Row.Iter(rs).map(_.as[String]("COLUMN_NAME")).toList
  }
}
