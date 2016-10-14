package models.result

import akka.actor.ActorRef
import models.database.{Query, Row}
import models.{QueryResultRowCount, ResponseMessage}
import services.result.CachedResultService
import utils.DateUtils

object CachedResultQuery {
  val maxRows = 50000
}

case class CachedResultQuery(index: Int, result: CachedResult, out: Option[ActorRef]) extends Query[ResponseMessage] {
  val startMs = DateUtils.nowMillis
  override val sql = result.sql

  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData

      val columns = CachedResultQueryHelper.getColumns(md)

      val columnsWithIndex = columns.zipWithIndex

      var rowCount = 1
      val firstRowData = CachedResultQueryHelper.dataFor(firstRow, columnsWithIndex)

      if (rows.hasNext) {
        val containsRowNum = columns.exists(_.name == "#")
        val cols = CachedResultInsert.insert(result, columns, containsRowNum)
        CachedResultQueryHelper.createResultTable(result.tableName, cols)
        val columnNames = cols.map(_.name)

        val transformedData = CachedResultTransform.transform(cols, if (containsRowNum) { firstRowData } else { Some(rowCount) +: firstRowData })
        CachedResultQueryHelper.insertRow(result.tableName, columnNames, transformedData)

        val partialRowData = collection.mutable.ArrayBuffer(transformedData)

        while (rowCount < CachedResultQuery.maxRows && rows.hasNext) {
          val row = rows.next()
          rowCount += 1
          val data = CachedResultQueryHelper.dataFor(row, columnsWithIndex)
          val transformedData = CachedResultTransform.transform(cols, if (containsRowNum) { data } else { Some(rowCount) +: data })

          CachedResultQueryHelper.insertRow(result.tableName, columnNames, transformedData)
          if (rowCount <= 100) {
            partialRowData += transformedData
          }
          if (rowCount == 101) {
            val firstMessageElapsed = (DateUtils.nowMillis - startMs).toInt
            CachedResultQueryHelper.sendResult(result, index, out, cols, partialRowData, firstMessageElapsed, moreRowsAvailable = true)
            CachedResultService.setFirstMessageDuration(result.resultId, firstMessageElapsed)
          }
        }

        if (rowCount <= 100) {
          val firstMessageElapsed = (DateUtils.nowMillis - startMs).toInt
          CachedResultService.setFirstMessageDuration(result.resultId, firstMessageElapsed)
          CachedResultQueryHelper.sendResult(result, index, out, cols, partialRowData, firstMessageElapsed, moreRowsAvailable = false)
        }

        val duration = (DateUtils.nowMillis - startMs).toInt
        CachedResultService.completeCacheResult(result.resultId, rowCount, duration)
        QueryResultRowCount(result.resultId, result.queryId, result.resultId, rowCount, rowCount == CachedResultQuery.maxRows, duration)
      } else {
        val elapsed = (DateUtils.nowMillis - startMs).toInt
        CachedResultQueryHelper.getResultResponseFor(result.resultId, index, result.queryId, result.sql, columns, Seq(firstRowData), elapsed)
      }
    } else {
      val elapsed = (DateUtils.nowMillis - startMs).toInt
      CachedResultQueryHelper.getResultResponseFor(result.resultId, index, result.queryId, result.sql, Nil, Nil, elapsed)
    }
  }
}
