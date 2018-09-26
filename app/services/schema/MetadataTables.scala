package services.schema

import java.sql.{Connection, DatabaseMetaData, Timestamp}
import java.util.UUID

import models.database.{Query, Row}
import models.engine.DatabaseEngine.{MySQL, PostgreSQL}
import models.schema.{EnumType, Table}
import services.database.DatabaseConnection
import util.{Logging, NullUtils}

import scala.util.control.NonFatal

object MetadataTables extends Logging {
  def getTables(connectionId: UUID, metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("TABLE"))
    new Row.Iter(rs).map(row => fromRow(connectionId, row)).toList.sortBy(_.name)
  }

  def withTableDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, tables: Seq[Table], enums: Seq[EnumType]) = {
    tables.zipWithIndex.map { table =>
      if (table._2 > 0 && table._2 % 25 == 0) { log.info(s"Processed [${table._2}/${tables.size}] tables...") }
      getTableDetails(db, conn, metadata, table._1, enums)
    }
  }

  private[this] def getTableDetails(db: DatabaseConnection, conn: Connection, metadata: DatabaseMetaData, table: Table, enums: Seq[EnumType]) = try {
    val definition = db.engine match {
      case MySQL => Some(db(conn, new Query[String] {
        override val sql = "show create table " + db.engine.cap.leftQuote + table.name + db.engine.cap.rightQuote
        override def reduce(rows: Iterator[Row]) = rows.map(_.as[String]("Create Table")).toList.headOption.getOrElse {
          throw new IllegalStateException("Missing column [Create Table].")
        }
      }))
      case _ => None
    }

    val rowStats = db.engine match {
      case MySQL => db(conn, new Query[Option[(String, Option[String], Long, Option[Int], Option[Long], Option[Long])]] {
        override val sql = s"""
          select table_name, engine, table_rows, avg_row_length, data_length, create_time
          from information_schema.tables where table_name = '${table.name}'
        """
        override def reduce(rows: Iterator[Row]) = rows.map { row =>
          val tableName = row.as[String]("table_name")
          val engine = row.asOpt[String]("engine").getOrElse("unknown")
          val rowEstimate = row.asOpt[Any]("table_rows").map(JdbcHelper.longVal).getOrElse(0L)
          val averageRowLength = row.asOpt[Any]("avg_row_length").map(JdbcHelper.intVal).getOrElse(0)
          val dataLength = row.asOpt[Any]("data_length").map(JdbcHelper.longVal).getOrElse(0L)
          val createTime = row.asOpt[Timestamp]("create_time").map(_.getTime)

          (tableName, Some(engine), rowEstimate, Some(averageRowLength), Some(dataLength), createTime)
        }.toList.headOption
      })
      case PostgreSQL => db(conn, new Query[Option[(String, Option[String], Long, Option[Int], Option[Long], Option[Long])]] {
        val t = s"""${table.schema.fold("")(_ + ".")}"${table.name}""""
        override val sql = s"select relname as name, reltuples as rows from pg_class where oid = '$t'::regclass"
        override def reduce(rows: Iterator[Row]) = rows.map { row =>
          val tableName = row.as[String]("name")
          val rowEstimate = JdbcHelper.longVal(row.as[Any]("rows"))

          (tableName, None, rowEstimate, None, None, None)
        }.toList.headOption
      })
      case _ => None
    }

    table.copy(
      definition = definition,
      storageEngine = rowStats.flatMap(_._2),
      rowCountEstimate = rowStats.map(_._3),
      averageRowLength = rowStats.flatMap(_._4),
      dataLength = rowStats.flatMap(_._5),
      columns = MetadataColumns.getColumns(metadata, table.catalog, table.schema, table.name, enums),
      rowIdentifier = MetadataIndentifiers.getRowIdentifier(metadata, table.catalog, table.schema, table.name),
      primaryKey = MetadataKeys.getPrimaryKey(metadata, table),
      foreignKeys = MetadataKeys.getForeignKeys(metadata, table),
      indexes = MetadataIndexes.getIndexes(metadata, table),
      createTime = rowStats.flatMap(_._6)
    )
  } catch {
    case NonFatal(x) =>
      log.warn(s"Unable to get table details for [${table.name}]", x)
      table
  }

  private[this] def fromRow(connectionId: UUID, row: Row) = Table(
    name = row.as[String]("TABLE_NAME"),
    connection = connectionId,
    catalog = row.asOpt[String]("TABLE_CAT"),
    schema = row.asOpt[String]("TABLE_SCHEM"),
    description = row.asOpt[String]("REMARKS"),
    definition = try {
      row.asOpt[String]("SQL")
    } catch {
      case NonFatal(_) => None
    }
  )
}
