package models.result

import akka.actor.ActorRef
import models.database.{Query, Row}
import models.query.QueryResult
import models.schema.ColumnType
import models.schema.ColumnType.LongType
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
        CachedResultService.insertCacheResult(result.copy(columns = columns.size))
        val containsRowNum = columns.exists(_.name == "#")
        val columnsPlus = if (containsRowNum) {
          columns
        } else {
          QueryResult.Col("#", LongType, None, None) +: columns
        }
        CachedResultQueryHelper.createResultTable(result.resultId, columnsPlus)
        val columnNames = columnsPlus.map(_.name)

        val transformedData = transform(columnsPlus, if (containsRowNum) { firstRowData } else { Some(rowCount) +: firstRowData })
        CachedResultQueryHelper.insertRow(result.tableName, columnNames, transformedData)

        val partialRowData = collection.mutable.ArrayBuffer(transformedData)

        while (rowCount < CachedResultQuery.maxRows && rows.hasNext) {
          val row = rows.next()
          rowCount += 1
          val data = CachedResultQueryHelper.dataFor(row, columnsWithIndex)
          val transformedData = transform(columnsPlus, if (containsRowNum) { data } else { Some(rowCount) +: data })

          CachedResultQueryHelper.insertRow(result.tableName, columnNames, transformedData)
          if (rowCount <= 100) {
            partialRowData += transformedData
          }
          if (rowCount == 101) {
            val firstMessageElapsed = (DateUtils.nowMillis - startMs).toInt
            CachedResultQueryHelper.sendResult(result, index, out, columnsPlus, partialRowData, firstMessageElapsed, moreRowsAvailable = true)
            CachedResultService.setFirstMessageDuration(result.resultId, firstMessageElapsed)
          }
        }

        if (rowCount <= 100) {
          val firstMessageElapsed = (DateUtils.nowMillis - startMs).toInt
          CachedResultService.setFirstMessageDuration(result.resultId, firstMessageElapsed)
          CachedResultQueryHelper.sendResult(result, index, out, columnsPlus, partialRowData, firstMessageElapsed, moreRowsAvailable = false)
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

  private[this] def transform(columns: Seq[QueryResult.Col], data: Seq[Option[Any]]) = columns.zip(data).map {
    case x if x._1.t == ColumnType.DateType && x._2.exists(_.isInstanceOf[String]) =>
      x._2.map(_.toString.stripSuffix(" 00:00:00"))
    case x => x._2
  }
}
