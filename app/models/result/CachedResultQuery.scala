package models.result

import java.util.UUID

import akka.actor.ActorRef
import models.QueryResultResponse
import models.database.{ Query, Row }
import models.queries.QueryTranslations
import models.queries.result.{ CreateResultTable, InsertResultRow }
import models.query.QueryResult
import services.database.ResultCacheDatabase

object CachedResultQuery {
  def createResultTable(resultId: UUID, columns: Seq[QueryResult.Col]) = {
    ResultCacheDatabase.conn.executeUpdate(CreateResultTable(resultId, columns)(ResultCacheDatabase.conn.engine))
  }

  def insertRow(tableName: String, columnNames: Seq[String], rowNum: Int, data: Seq[Any]) = {
    ResultCacheDatabase.conn.executeUpdate(InsertResultRow(tableName, columnNames, rowNum +: data)(ResultCacheDatabase.conn.engine))
  }
}

case class CachedResultQuery(result: CachedResult, out: Option[ActorRef]) extends Query[Int] {
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

      val columnNames = "row_num" +: columns.map(_.name)

      var rowCount = 1
      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i))
      CachedResultQuery.insertRow(result.tableName, columnNames, rowCount, firstRowData)

      out.foreach { o =>
        val msg = QueryResult(
          queryId = result.queryId,
          sql = result.sql,
          columns = columns,
          data = Seq(firstRowData.map(_.map(_.toString))),
          moreRowsAvailable = rows.hasNext,
          source = Some(QueryResult.Source("cache", result.tableName))
        )

        o ! QueryResultResponse(result.resultId, Seq(msg), 0)
      }

      rows.foreach { row =>
        rowCount += 1
        val data = (1 to cc).map(i => row.asOpt[Any](i))
        CachedResultQuery.insertRow(result.tableName, columnNames, rowCount, data)
      }

      rowCount
    } else {
      0
    }
  }
}
