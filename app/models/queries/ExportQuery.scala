package models.queries

import models.database.{ Query, Row }
import models.schema.ColumnType

case class ExportQuery(override val sql: String, format: String) extends Query[(Seq[(String, ColumnType)], Iterator[Seq[Option[String]]])] {
  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val columnType = QueryTranslations.forType(md.getColumnType(i))
        md.getColumnLabel(i) -> columnType
      }
      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i).map(_.toString))
      val remainingData = rows.map { row =>
        (1 to cc).map(i => row.asOpt[Any](i).map(_.toString))
      }

      val data = Iterator(firstRowData) ++ remainingData
      columns -> data
    } else {
      Seq.empty -> Iterator.empty
    }
  }
}
