package models.queries.export

import java.io.OutputStream

import models.database.{ Query, Row }

case class XlsxExportQuery(override val sql: String, format: String, out: OutputStream) extends Query[Unit] {
  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map(md.getColumnLabel)
      out.write((columns.mkString(",") + "\n").getBytes)

      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i).map(_.toString).getOrElse(""))
      out.write((firstRowData.mkString(",") + "\n").getBytes)

      val remainingData = rows.foreach { row =>
        val data = (1 to cc).map(i => row.asOpt[Any](i).map(_.toString).getOrElse(""))
        out.write((data.mkString(",") + "\n").getBytes)
      }
    } else {
      out.write("".getBytes)
    }
    out.close()
  }
}
