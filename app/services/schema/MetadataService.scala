package services.schema

import java.sql.DatabaseMetaData
import javax.sql.DataSource

import models.database.Row
import models.schema.{ IndexColumn, Index, Column, Table }
import utils.NullUtils

object MetadataService {
  def getMetadata(source: DataSource) = {
    val conn = source.getConnection()

    val catalog = Option(conn.getCatalog)
    val schema = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val tables = getTables(metadata, catalog, schema)

    val tablesWithColumns = tables.map { t =>
      val columns = getColumns(metadata, t)
      t.copy(columns = columns)
    }

    val tablesWithIndexes = tablesWithColumns.map { t =>
      val indices = getIndices(metadata, t)
      t.copy(indices = indices)
    }

    conn.close()

    tablesWithIndexes
  }

  private[this] def getTables(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String]) = {
    val rs = metadata.getTables(catalog.orNull, schema.orNull, NullUtils.inst, Array("TABLE"))
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

  private[this] def getColumns(metadata: DatabaseMetaData, t: Table) = {
    val rs = metadata.getColumns(t.catalog.orNull, t.schema.orNull, t.name, NullUtils.inst)
    val columns = new Row.Iter(rs).map { row =>
      row.as[Int]("ORDINAL_POSITION") -> Column(
        name = row.as[String]("COLUMN_NAME"),
        description = row.asOpt[String]("REMARKS"),
        definition = row.asOpt[String]("COLUMN_DEF"),
        primaryKey = false, //row.as[Boolean]("?"),
        notNull = row.as[Int]("NULLABLE") == 0, // IS_NULLABLE?
        autoIncrement = row.as[String]("IS_AUTOINCREMENT") == "YES",
        typeCode = row.as[Int]("DATA_TYPE"), // SQL_DATA_TYPE? SOURCE_DATA_TYPE?
        typeName = row.as[String]("TYPE_NAME"),
        size = row.asOpt[Int]("COLUMN_SIZE").map(_.toString).getOrElse("?"),
        sizeAsInt = row.asOpt[Int]("COLUMN_SIZE").getOrElse(0), // ?
        scale = 0, // BUFFER_LENGTH? DECIMAL_DIGITS? NUM_PREC_RADIX?
        defaultValue = None // row.asOpt[String]("?")
      )
    }.toList
    columns.sortBy(_._1).map(_._2)
  }

  private[this] def getIndices(metadata: DatabaseMetaData, t: Table) = {
    val rs = metadata.getIndexInfo(t.catalog.orNull, t.schema.orNull, t.name, false, false)
    val indexColumns = new Row.Iter(rs).map { row =>
      // [index_qualifier], [pages], [filter_condition]
      val name = row.as[String]("index_name")
      val unique = !row.as[Boolean]("non_unique")
      val position = row.as[Int]("ordinal_position")
      val ascending = row.asOpt[String]("asc_or_desc").getOrElse("A") == "A"

      val columnName = row.as[String]("column_name")
      val typ = row.as[Int]("type") match {
        case DatabaseMetaData.tableIndexStatistic => "statistic"
        case DatabaseMetaData.tableIndexClustered => "clustered"
        case DatabaseMetaData.tableIndexHashed => "hashed"
        case DatabaseMetaData.tableIndexOther => "other"
        case x => throw new IllegalArgumentException(x.toString)
      }
      val cardinality = row.as[Any]("cardinality") match {
        case l: Long => l
        case f: Float => f.toLong
        case x => throw new IllegalArgumentException(x.getClass.getName)
      }
      (name, unique, typ, cardinality, position, columnName, ascending)
    }.toList

    val indices = indexColumns.groupBy(_._1).map { cols =>
      val idxCols = cols._2.sortBy(_._5).map { col =>
        IndexColumn(name = col._6, ascending = col._7)
      }
      val idxInfo = cols._2.head
      Index(
        name = idxInfo._1,
        unique = idxInfo._2,
        indexType = idxInfo._3,
        cardinality = idxInfo._4,
        columns = idxCols
      )
    }.toSeq

    indices.sortBy(_.name)
  }
}
