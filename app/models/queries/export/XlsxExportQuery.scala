package models.queries.export

import java.io.OutputStream
import java.sql.{Date, Time, Timestamp}
import java.util.GregorianCalendar

import models.database.{Query, Row}
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.WorkbookUtil
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import utils.NullUtils

case class XlsxExportQuery(title: String, override val sql: String, format: String, out: OutputStream) extends Query[Unit] {
  override def reduce(rows: Iterator[Row]) = {
    val wb = new SXSSFWorkbook(100)

    val resultSheet = wb.createSheet(WorkbookUtil.createSafeSheetName(title))

    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map(md.getColumnLabel)
      val resultHeader = resultSheet.createRow(0)
      columns.zipWithIndex.foreach { col =>
        resultHeader.createCell(col._2).setCellValue(col._1)
      }

      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i))
      val firstSheetRow = resultSheet.createRow(1)
      firstRowData.zipWithIndex.foreach { data =>
        setCell(firstSheetRow.createCell(data._2), data._1)
      }

      rows.zipWithIndex.foreach { row =>
        val data = (1 to cc).map(i => row._1.asOpt[Any](i))
        val sheetRow = resultSheet.createRow(row._2 + 2)
        data.zipWithIndex.foreach { data =>
          setCell(sheetRow.createCell(data._2), data._1)
        }
      }
    }

    val sqlSheet = wb.createSheet("SQL")
    val sqlRow = sqlSheet.createRow(0)
    sqlRow.setHeight(100)
    sqlRow.createCell(0).setCellValue(sql)

    wb.write(out)
  }

  private[this] def setCell(cell: Cell, v: Option[Any]) = v match {
    case Some(x) => x match {
      case s: String => cell.setCellValue(s)

      case b: Boolean => cell.setCellValue(b)

      case bd: BigDecimal => cell.setCellValue(bd.toDouble)
      case b: Byte => cell.setCellValue(b.toDouble)
      case s: Short => cell.setCellValue(s.toDouble)
      case i: Integer => cell.setCellValue(i.toDouble)
      case l: Long => cell.setCellValue(l.toDouble)
      case f: Float => cell.setCellValue(f.toDouble)
      case d: Double => cell.setCellValue(d)

      case d: Date =>
        val cal = new GregorianCalendar()
        cal.setTime(d)
        cell.setCellValue(cal)
      case t: Time => cell.setCellValue(t.toString)
      case ts: Timestamp =>
        val cal = new GregorianCalendar()
        cal.setTimeInMillis(ts.getTime)
        cell.setCellValue(cal)

      case n if n == NullUtils.inst => // no op
      case other => cell.setCellValue(other.toString)

    }
    case None => // no op
  }
}
