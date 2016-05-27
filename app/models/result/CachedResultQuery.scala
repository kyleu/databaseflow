package models.result

import akka.actor.ActorRef
import models.database.{ Query, Row }
import models.queries.QueryTranslations
import models.query.QueryResult
import models.schema.ColumnType.LongType
import models.{ QueryResultRowCount, ResponseMessage }
import services.result.CachedResultService
import utils.DateUtils

case class CachedResultQuery(result: CachedResult, out: Option[ActorRef]) extends Query[ResponseMessage] {
  val startMs = DateUtils.nowMillis

  override def sql: String = result.sql

  def dataFor(row: Row, cc: Int) = (1 to cc).map(i => row.asOpt[Any](i) match {
    case x => x
  })

  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData

      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val columnType = QueryTranslations.forType(md.getColumnType(i))
        QueryResult.Col(md.getColumnLabel(i), columnType, md.getPrecision(i), md.getScale(i))
      }

      val containsRowNum = columns.exists(_.name == "#")

      val updatedColumns = if (containsRowNum) {
        columns
      } else {
        QueryResult.Col("#", LongType, 0, 0) +: columns
      }

      val columnNames = updatedColumns.map(_.name)

      var rowCount = 1
      val firstRowData = dataFor(firstRow, cc)

      if (rows.hasNext) {
        CachedResultService.insertCacheResult(result.copy(columns = columns.size))
        CachedResultQueryHelper.createResultTable(result.resultId, columns)

        val transformedData = if (containsRowNum) {
          firstRowData
        } else {
          Some(rowCount) +: firstRowData
        }

        CachedResultQueryHelper.insertRow(result.tableName, columnNames, transformedData)

        val partialRowData = collection.mutable.ArrayBuffer(transformedData)

        rows.foreach { row =>
          rowCount += 1
          val data = dataFor(row, cc)
          val transformedData = if (containsRowNum) {
            data
          } else {
            Some(rowCount) +: data
          }

          CachedResultQueryHelper.insertRow(result.tableName, columnNames, transformedData)
          if (rowCount <= 100) {
            partialRowData += transformedData
          }
          if (rowCount == 101) {
            CachedResultQueryHelper.sendResult(result, out, updatedColumns, partialRowData, (DateUtils.nowMillis - startMs).toInt, moreRowsAvailable = true)
          }
        }

        if (rowCount <= 100) {
          CachedResultQueryHelper.sendResult(result, out, updatedColumns, partialRowData, (DateUtils.nowMillis - startMs).toInt, moreRowsAvailable = false)
        }

        val duration = (DateUtils.nowMillis - startMs).toInt
        CachedResultService.completeCacheResult(result.resultId, rowCount, duration)
        QueryResultRowCount(result.resultId, result.queryId, rowCount, duration)
      } else {
        CachedResultQueryHelper.getResultResponseFor(result.resultId, result.queryId, result.sql, columns, Seq(firstRowData))
      }
    } else {
      CachedResultQueryHelper.getResultResponseFor(result.resultId, result.queryId, result.sql, Nil, Nil)
    }
  }
}
