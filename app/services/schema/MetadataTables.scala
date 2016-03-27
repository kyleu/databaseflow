package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.Table
import utils.NullUtils

object MetadataTables {
  def getTableNames(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], tableType: String) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array(tableType))
    val tableNames = new Row.Iter(rs).map(row => row.as[String]("TABLE_NAME")).toList
    tableNames.sorted
  }

  def getTableDetails(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], tableName: String) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, tableName, NullUtils.inst)
    val tables = new Row.Iter(rs).map(fromRow).toList
    val t = tables.headOption.getOrElse(throw new IllegalArgumentException(s"Cannot find table [$tableName]."))

    t.copy(
      columns = MetadataColumns.getColumns(metadata, t),
      rowIdentifier = MetadataIndentifiers.getRowIdentifier(metadata, t),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, t),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, t),
      indexes = MetadataIndexes.getIndexes(metadata, t)
    )
  }

  private[this] def fromRow(row: Row) = Table(
    name = row.as[String]("TABLE_NAME"),
    catalog = row.asOpt[String]("TABLE_CAT"),
    schema = row.asOpt[String]("TABLE_SCHEM"),
    description = row.asOpt[String]("REMARKS"),
    definition = None,
    typeName = row.as[String]("TABLE_TYPE")
  )
}
