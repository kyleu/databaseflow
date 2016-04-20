package services.schema

import java.sql.{ Connection, DatabaseMetaData, Timestamp }

import models.database.{ Query, Row }
import models.engine.rdbms.MySQL
import models.schema.Table
import services.database.DatabaseConnection
import utils.NullUtils

import scala.util.control.NonFatal

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
        override def sql = "show create table " + db.engine.leftQuoteIdentifier + table.name + db.engine.rightQuoteIdentifier
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create Table")).toList.head
      }))
      case _ => None
    }

    val rowStats = db.engine match {
      case MySQL => db(conn, new Query[Option[(String, String, Long, Int, Long, Long)]] {
        override def sql = {
          s"""select table_name, engine, table_rows, avg_row_length, data_length, create_time from information_schema.tables where table_name = \"${table.name}\""""
        }
        override def reduce(rows: Iterator[Row]) = rows.map { row =>
          val tableName = row.as[String]("table_name")
          val engine = row.as[String]("engine")
          val rowEstimate = JdbcHelper.longVal(row.as[Any]("table_rows"))
          val averageRowLength = JdbcHelper.intVal(row.as[Any]("avg_row_length"))
          val dataLength = JdbcHelper.longVal(row.as[Any]("data_length"))
          val createTime = row.as[Timestamp]("create_time").getTime

          (tableName, engine, rowEstimate, averageRowLength, dataLength, createTime)
        }.toList.headOption
      })
      case _ => None
    }

    table.copy(
      definition = definition,

      storageEngine = rowStats.map(_._2),

      rowCountEstimate = rowStats.map(_._3),
      averageRowLength = rowStats.map(_._4),
      dataLength = rowStats.map(_._5),

      columns = MetadataColumns.getColumns(metadata, table.catalog, table.schema, table.name),
      rowIdentifier = MetadataIndentifiers.getRowIdentifier(metadata, table.catalog, table.schema, table.name),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, table),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, table),
      indexes = MetadataIndexes.getIndexes(metadata, table),

      createTime = rowStats.map(_._6)
    )
  }

  private[this] def fromRow(row: Row) = {
    val definition = try {
      row.asOpt[String]("SQL")
    } catch {
      case NonFatal(x) => None
    }
    Table(
      name = row.as[String]("TABLE_NAME"),
      catalog = row.asOpt[String]("TABLE_CAT"),
      schema = row.asOpt[String]("TABLE_SCHEM"),
      description = row.asOpt[String]("REMARKS"),
      definition = definition
    )
  }
}
