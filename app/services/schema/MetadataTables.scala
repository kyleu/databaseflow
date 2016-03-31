package services.schema

import java.sql.{ Connection, DatabaseMetaData }

import models.database.{ Query, Row }
import models.engine.DatabaseEngine
import models.schema.Table
import services.database.DatabaseConnection
import utils.NullUtils

object MetadataTables {
  def getTableNames(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], tableType: String) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array(tableType))
    val tableNames = new Row.Iter(rs).map(row => row.as[String]("TABLE_NAME")).toList
    tableNames.sorted
  }

  def getTableDetails(
    db: DatabaseConnection,
    conn: Connection,
    metadata: DatabaseMetaData,
    catalog: Option[String],
    schema: Option[String],
    tableName: String
  ) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, tableName, NullUtils.inst)
    val tables = new Row.Iter(rs).map(fromRow).toList
    val t = tables.headOption.getOrElse(throw new IllegalArgumentException(s"Cannot find table [$tableName]."))

    val definition = if (db.engine.showCreateTableSupported) {
      Some(db(conn, new Query[String] {
        override def sql = db.engine.showCreateTable(tableName)
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create Table")).toList.head
      }))
    } else {
      None
    }

    t.copy(
      definition = definition,
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
