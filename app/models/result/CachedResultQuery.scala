package models.result

import java.util.UUID

import akka.actor.ActorRef
import models.{ QueryResultResponse, QueryResultRowCount }
import models.database.{ Query, Row }
import models.queries.{ DynamicQuery, QueryTranslations }
import models.queries.result.{ CreateResultTable, InsertResultRow }
import models.query.QueryResult
import services.database.ResultCacheDatabase
import utils.DateUtils

object CachedResultQuery {
  def createResultTable(resultId: UUID, columns: Seq[QueryResult.Col]) = {
    ResultCacheDatabase.conn.executeUpdate(CreateResultTable(resultId, columns)(ResultCacheDatabase.conn.engine))
  }

  def insertRow(tableName: String, columnNames: Seq[String], data: Seq[Any]) = {
    ResultCacheDatabase.conn.executeUpdate(InsertResultRow(tableName, columnNames, data)(ResultCacheDatabase.conn.engine))
  }
}

case class CachedResultQuery(result: CachedResult, out: Option[ActorRef]) extends Query[QueryResultRowCount] {
  val startMs = DateUtils.nowMillis

  override def sql: String = result.sql

  override def reduce(rows: Iterator[Row]) = {
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData

      val cc = md.getColumnCount
      val columns = (1 to cc).map { i =>
        val columnType = QueryTranslations.forType(md.getColumnType(i))
        QueryResult.Col(md.getColumnLabel(i), columnType, md.getPrecision(i), md.getScale(i))
      }
      CachedResultQuery.createResultTable(result.resultId, columns)

      val appendRowNum = !columns.exists(_.name == "row_num")

      val columnNames = if (appendRowNum) {
        "row_num" +: columns.map(_.name)
      } else {
        columns.map(_.name)
      }

      var rowCount = 1
      val firstRowData = if (appendRowNum) {
        Some(rowCount) +: (1 to cc).map(i => firstRow.asOpt[Any](i))
      } else {
        (1 to cc).map(i => firstRow.asOpt[Any](i))
      }
      CachedResultQuery.insertRow(result.tableName, columnNames, firstRowData)

      val partialRowData = collection.mutable.ArrayBuffer(firstRowData)

      rows.foreach { row =>
        rowCount += 1
        val data = if (appendRowNum) {
          Some(rowCount) +: (1 to cc).map(i => row.asOpt[Any](i))
        } else {
          (1 to cc).map(i => row.asOpt[Any](i))
        }
        CachedResultQuery.insertRow(result.tableName, columnNames, data)
        if (rowCount <= 100) {
          partialRowData += data
        }
        if (rowCount == 101) {
          sendResult(columns, partialRowData, (DateUtils.nowMillis - startMs).toInt, moreRowsAvailable = true)
        }
      }

      if (rowCount <= 100) {
        sendResult(columns, partialRowData, (DateUtils.nowMillis - startMs).toInt, moreRowsAvailable = false)
      }

      QueryResultRowCount(result.resultId, result.queryId, rowCount, (DateUtils.nowMillis - startMs).toInt)
    } else {
      sendResult(Nil, Nil, 0, moreRowsAvailable = false)
      QueryResultRowCount(result.resultId, result.queryId, 0, (DateUtils.nowMillis - startMs).toInt)
    }
  }

  private[this] def sendResult(columns: Seq[QueryResult.Col], rowData: Seq[Seq[Option[Any]]], elapsedMs: Int, moreRowsAvailable: Boolean) = {
    out.foreach { o =>
      val msg = QueryResultResponse(result.resultId, QueryResult(
        queryId = result.queryId,
        sql = result.sql,
        columns = columns,
        data = rowData.map(_.map(_.map(DynamicQuery.transform))),
        moreRowsAvailable = moreRowsAvailable,
        source = Some(QueryResult.Source(
          t = "cache",
          name = result.tableName,
          sortable = true
        ))
      ), 0)
      o ! msg
    }
  }
}
