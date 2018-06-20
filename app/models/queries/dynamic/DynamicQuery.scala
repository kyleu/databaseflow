package models.queries.dynamic

import java.sql.ResultSetMetaData
import java.util.UUID

import models.database.{Query, Row}
import models.queries.QueryTranslations
import models.query.QueryResult
import models.schema.{ColumnType, EnumType}
import org.h2.jdbc.JdbcClob

object DynamicQuery {
  case class Results(
      cols: Seq[QueryResult.Col],
      data: Seq[Seq[Option[String]]]
  )

  def transform(x: Any) = x match {
    case c: JdbcClob => c.getSubString(1, c.length().toInt)
    case a: Array[Byte] if a.length == 16 => UUID.nameUUIDFromBytes(a).toString
    case _ => x.toString
  }

  def getColumnMetadata(md: ResultSetMetaData, colIdx: Int, enums: Seq[EnumType]) = {
    val columnType: ColumnType = QueryTranslations.forType(md.getColumnType(colIdx), md.getColumnTypeName(colIdx), None, enums)
    val precision = md.getPrecision(colIdx) match {
      case x if x < 1 => None
      case x => Some(x)
    }
    val scale = md.getScale(colIdx) match {
      case x if x < 1 => None
      case x => Some(x)
    }
    (columnType, precision, scale)
  }
}

case class DynamicQuery(override val sql: String, override val values: Seq[Any] = Nil, enums: Seq[EnumType] = Nil) extends Query[DynamicQuery.Results] {
  override def reduce(rows: Iterator[Row]) = getResults(rows)

  private[this] def rowData(cc: Int, row: Row) = (1 to cc).map(i => row.asOpt[Any](i).map(DynamicQuery.transform))

  private[this] def getResults(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val (columnType, precision, scale) = DynamicQuery.getColumnMetadata(md, i, enums)
        QueryResult.Col(md.getColumnLabel(i), columnType, precision, scale)
      }
      val firstRowData = rowData(cc, firstRow)
      val remainingData = rows.map(rowData(cc, _)).toList

      val data = firstRowData +: remainingData
      DynamicQuery.Results(columns, data)
    } else {
      DynamicQuery.Results(Nil, Nil)
    }
  }
}
