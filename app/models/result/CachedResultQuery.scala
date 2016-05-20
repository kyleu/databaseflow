package models.result

import models.database.{ Query, Row }
import models.queries.QueryTranslations
import models.queries.result.{ CreateResultTable, InsertResultRow }
import services.database.ResultCacheDatabase

case class CachedResultQuery(result: CachedResult) extends Query[Int] {
  override def sql: String = result.sql

  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData

      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val columnType = QueryTranslations.forType(md.getColumnType(i))
        (md.getColumnLabel(i), columnType)
      }
      ResultCacheDatabase.conn.executeUpdate(CreateResultTable(result.resultId, columns)(ResultCacheDatabase.conn.engine))
      val columnNames = "row_num" +: columns.map(_._1)

      var rowCount = 1
      val firstRowData = rowCount +: (1 to cc).map(i => firstRow.asOpt[Any](i))
      ResultCacheDatabase.conn.executeUpdate(InsertResultRow(result.tableName, columnNames, firstRowData)(ResultCacheDatabase.conn.engine))

      rows.foreach { row =>
        rowCount += 1
        val data = rowCount +: (1 to cc).map(i => row.asOpt[Any](i))
        ResultCacheDatabase.conn.executeUpdate(InsertResultRow(result.tableName, columnNames, data)(ResultCacheDatabase.conn.engine))
      }

      rowCount
    } else {
      0
    }
  }
}
