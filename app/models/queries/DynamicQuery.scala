package models.queries

import models.database.{ Query, Row }
import models.query.QueryResult

case class DynamicQuery(override val sql: String) extends Query[(Seq[QueryResult.Col], Seq[Seq[Option[String]]])] {
  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val columnType = QueryTranslations.forType(md.getColumnType(i))
        QueryResult.Col(md.getColumnLabel(i), columnType)
      }
      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i).map(_.toString))
      val remainingData = rows.map { row =>
        (1 to cc).map(i => row.asOpt[Any](i).map(_.toString))
      }.toList

      val data = firstRowData +: remainingData

      columns -> data
    } else {
      Nil -> Nil
    }
  }
}
