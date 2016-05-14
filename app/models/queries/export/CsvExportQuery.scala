package models.queries.export

import java.io.OutputStream

import com.github.tototoshi.csv.CSVWriter
import models.database.{ Query, Row }

case class CsvExportQuery(override val sql: String, format: String, out: OutputStream) extends Query[Unit] {
  override def reduce(rows: Iterator[Row]) = {
    val writer = CSVWriter.open(out)
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map(md.getColumnLabel)
      writer.writeRow(columns)

      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i).map(_.toString).getOrElse(""))
      writer.writeRow(firstRowData)

      rows.foreach { row =>
        val data = (1 to cc).map(i => row.asOpt[Any](i).map(_.toString).getOrElse(""))
        writer.writeRow(data)
      }
    }
    writer.close()
  }
}
