package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.ClientInfoProperty

object MetadataClientInfoProperties {
  def getClientInfoProperties(metadata: DatabaseMetaData) = {
    val rs = metadata.getClientInfoProperties
    val functions = new Row.Iter(rs).map(fromRow).toList
    functions.sortBy(_.name)
  }

  private[this] def fromRow(row: Row) = ClientInfoProperty(
    name = row.as[String]("NAME"),
    description = row.asOpt[String]("DESCRIPTION"),
    maxLength = row.as[Int]("MAX_LEN"),
    defaultValue = row.asOpt[String]("DEFAULT_VALUE")
  )
}
