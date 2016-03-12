package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.{ Index, IndexColumn, Table }

object MetadataIndices {
  def getIndices(metadata: DatabaseMetaData, t: Table) = {
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
