package services.schema

import java.sql.{ Connection, DatabaseMetaData }

import models.database.{ Query, Row }
import models.engine.rdbms.MySQL
import models.schema.Table
import services.database.DatabaseConnection
import utils.NullUtils

object MetadataTables {
  def getTables(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("TABLE"))
    new Row.Iter(rs).map(fromRow).toList.sortBy(_.name)
  }

  def withTableDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, tables: Seq[Table]) = tables.map { table =>
    getTableDetails(db, conn, metadata, table)
  }

  private[this] def getTableDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, table: Table) = {
    val definition = db.engine match {
      case MySQL => Some(db(conn, new Query[String] {
        override def sql = "show create table " + table.name
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create Table")).toList.head
      }))
      case _ => None
    }

    table.copy(
      definition = definition,
      columns = MetadataColumns.getColumns(metadata, table.catalog, table.schema, table.name),
      rowIdentifier = MetadataIndentifiers.getRowIdentifier(metadata, table.catalog, table.schema, table.name),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, table),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, table),
      indexes = MetadataIndexes.getIndexes(metadata, table)
    )
  }

  private[this] def fromRow(row: Row) = {
    println(utils.JdbcUtils.rowToString(row))
    Table(
      name = row.as[String]("TABLE_NAME"),
      catalog = row.asOpt[String]("TABLE_CAT"),
      schema = row.asOpt[String]("TABLE_SCHEM"),
      description = row.asOpt[String]("REMARKS"),
      definition = None
    )
  }
}
