package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.Table
import utils.NullUtils

object MetadataTables {
  def getTables(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], tableType: String) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array(tableType))
    val tables = new Row.Iter(rs).map { row =>
      Table(
        name = row.as[String]("TABLE_NAME"),
        catalog = row.asOpt[String]("TABLE_CAT"),
        schema = row.asOpt[String]("TABLE_SCHEM"),
        description = row.asOpt[String]("REMARKS"),
        definition = None,
        typeName = row.as[String]("TABLE_TYPE")
      )
    }.toList
    tables.sortBy(_.name)
  }
}
