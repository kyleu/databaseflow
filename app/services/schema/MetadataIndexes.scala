package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.{ Index, IndexColumn, Table }

object MetadataIndexes {
  def getIndexes(metadata: DatabaseMetaData, t: Table) = {
    val rs = metadata.getIndexInfo(t.catalog.orNull, t.schema.orNull, t.name, false, false)
    val indexColumns = new Row.Iter(rs).map(fromRow).toList
    val indexes = indexColumns.groupBy(_._1).map { cols =>
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

    indexes.sortBy(_.name)
  }

  private[this] def fromRow(row: Row) = {
    // [index_qualifier], [pages], [filter_condition]
    val name = row.asOpt[String]("index_name").getOrElse("null")
    val nonUnique = row.asOpt[Any]("non_unique").exists {
      case b: Boolean => b
      case bd: java.math.BigDecimal => bd.intValue > 0
    }
    val position = row.asOpt[Any]("ordinal_position").map(JdbcHelper.intVal).getOrElse(0)
    val ascending = row.asOpt[String]("asc_or_desc").getOrElse("A") == "A"

    val columnName = row.asOpt[String]("column_name").getOrElse("null")
    val typ = JdbcHelper.intVal(row.as[Int]("type")) match {
      case DatabaseMetaData.tableIndexStatistic => "statistic"
      case DatabaseMetaData.tableIndexClustered => "clustered"
      case DatabaseMetaData.tableIndexHashed => "hashed"
      case DatabaseMetaData.tableIndexOther => "other"
      case x => throw new IllegalArgumentException(x.toString)
    }
    val cardinality = row.as[Any]("cardinality") match {
      case l: Long => l
      case f: Float => f.toLong
      case i: Int => i.toLong
      case bd: java.math.BigDecimal => bd.longValue
      case x => throw new IllegalArgumentException(x.getClass.getName)
    }
    (name, !nonUnique, typ, cardinality, position, columnName, ascending)
  }
}
