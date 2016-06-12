package models.queries

import java.sql.ResultSetMetaData

import models.database.{Query, Row}
import models.query.QueryResult

object DynamicQuery {
  case class Results(
    cols: Seq[QueryResult.Col],
    data: Seq[Seq[Option[String]]]
  )

  def transform(x: Any) = x match {
    case _ => x.toString
  }

  def getColumnMetadata(md: ResultSetMetaData, colIdx: Int) = {
    val columnType = QueryTranslations.forType(md.getColumnType(colIdx))
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

case class DynamicQuery(override val sql: String) extends Query[DynamicQuery.Results] {
  override def reduce(rows: Iterator[Row]) = getResults(rows)

  private[this] def rowData(cc: Int, firstRow: Row) = (1 to cc).map(i => firstRow.asOpt[Any](i).map(DynamicQuery.transform))

  private[this] def getResults(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val (columnType, precision, scale) = DynamicQuery.getColumnMetadata(md, i)
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
