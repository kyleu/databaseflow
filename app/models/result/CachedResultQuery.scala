package models.result

import akka.actor.ActorRef
import models.database.{ Query, Row }
import models.queries.QueryTranslations
import models.query.QueryResult
import models.schema.ColumnType.{ ArrayType, LongType, UnknownType }
import models.{ QueryResultRowCount, ResponseMessage }
import services.result.CachedResultService
import utils.DateUtils

case class CachedResultQuery(result: CachedResult, out: Option[ActorRef]) extends Query[ResponseMessage] {
  val startMs = DateUtils.nowMillis

  override def sql: String = result.sql

  def dataFor(row: Row, columns: Seq[(QueryResult.Col, Int)]) = columns.map(c => row.asOpt[Any](c._2 + 1) match {
    case Some(x) if c._1.t == ArrayType => Some(x.toString)
    case Some(x) if c._1.t == UnknownType => Some(x.toString)
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
      val columnsWithIndex = columns.zipWithIndex

      val containsRowNum = columns.exists(_.name == "#")

      val updatedColumns = if (containsRowNum) {
        columns
      } else {
        QueryResult.Col("#", LongType, 0, 0) +: columns
      }

      val columnNames = updatedColumns.map(_.name)

      var rowCount = 1
      val firstRowData = dataFor(firstRow, columnsWithIndex)

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
          val data = dataFor(row, columnsWithIndex)
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
            val firstMessageElapsed = (DateUtils.nowMillis - startMs).toInt
            CachedResultService.setFirstMessageDuration(result.resultId, firstMessageElapsed)
            CachedResultQueryHelper.sendResult(result, out, updatedColumns, partialRowData, firstMessageElapsed, moreRowsAvailable = true)
          }
        }

        if (rowCount <= 100) {
          val firstMessageElapsed = (DateUtils.nowMillis - startMs).toInt
          CachedResultService.setFirstMessageDuration(result.resultId, firstMessageElapsed)
          CachedResultQueryHelper.sendResult(result, out, updatedColumns, partialRowData, firstMessageElapsed, moreRowsAvailable = false)
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
