package models.queries

import java.sql.PreparedStatement

import models.database.{ Query, Row }
import models.query.QueryResult

object DynamicQuery {
  case class Results(
    cols: Seq[QueryResult.Col],
    data: Seq[Seq[Option[String]]]
  )

  def transform(x: Any) = x match {
    case _ => x.toString
  }
}

case class DynamicQuery(override val sql: String) extends Query[Seq[DynamicQuery.Results]] {
  override def reduce(stmt: PreparedStatement, rows: Iterator[Row]) = {
    val firstResults = getResults(rows: Iterator[Row])
    var ret = Seq(firstResults)
    if (stmt.getMoreResults()) {
      ret = ret :+ getResults(rows)
    }
    ret
  }

  private[this] def rowData(cc: Int, firstRow: Row) = (1 to cc).map(i => firstRow.asOpt[Any](i).map(DynamicQuery.transform))

  private[this] def getResults(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val columnType = QueryTranslations.forType(md.getColumnType(i))
        QueryResult.Col(md.getColumnLabel(i), columnType, md.getPrecision(i), md.getScale(i))
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
