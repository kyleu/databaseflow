package models.queries

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

case class DynamicQuery(override val sql: String) extends Query[DynamicQuery.Results] {
  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val columnType = QueryTranslations.forType(md.getColumnType(i))
        QueryResult.Col(md.getColumnLabel(i), columnType, md.getPrecision(i), md.getScale(i))
      }
      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i).map(DynamicQuery.transform))
      val remainingData = rows.map { row =>
        (1 to cc).map(i => row.asOpt[Any](i).map(_.toString))
      }.toList

      val data = firstRowData +: remainingData
      DynamicQuery.Results(columns, data)
    } else {
      DynamicQuery.Results(Nil, Nil)
    }
  }
}
