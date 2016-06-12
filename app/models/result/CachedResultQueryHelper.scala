package models.result

import java.sql.ResultSetMetaData
import java.util.UUID

import akka.actor.ActorRef
import models.QueryResultResponse
import models.database.Row
import models.queries.DynamicQuery
import models.queries.result.{CreateResultTable, InsertResultRow}
import models.query.QueryResult
import models.schema.ColumnType.{ArrayType, UnknownType}
import services.database.ResultCacheDatabase
import utils.Logging

object CachedResultQueryHelper extends Logging {
  def createResultTable(resultId: UUID, columns: Seq[QueryResult.Col]) = {
    ResultCacheDatabase.conn.executeUpdate(CreateResultTable(resultId, columns)(ResultCacheDatabase.conn.engine))
  }

  def getColumns(md: ResultSetMetaData) = {
    val cc = md.getColumnCount
    val columns = (1 to cc).map { i =>
      val (columnType, precision, scale) = DynamicQuery.getColumnMetadata(md, i)
      val label = md.getColumnLabel(i)
      QueryResult.Col(label, columnType, precision, scale)
    }

    val dedupedColumns = columns.foldLeft(Seq.empty[QueryResult.Col]) { (x, y) =>
      if (x.exists(_.name == y.name)) {
        val idx = (2 to 20).find(i => !x.exists(_.name == y.name + i))
        x :+ y.copy(name = y.name + idx.getOrElse(""))
      } else {
        x :+ y
      }
    }
    dedupedColumns
  }

  def dataFor(row: Row, columns: Seq[(QueryResult.Col, Int)]) = columns.map(c => row.asOpt[Any](c._2 + 1) match {
    case Some(x) if c._1.t == ArrayType => Some(x.toString)
    case Some(x) if c._1.t == UnknownType => Some(x.toString)
    case x => x
  })

  def insertRow(tableName: String, columnNames: Seq[String], data: Seq[Any]) = {
    ResultCacheDatabase.conn.executeUpdate(InsertResultRow(tableName, columnNames, data)(ResultCacheDatabase.conn.engine))
  }

  def getResultResponseFor(resultId: UUID, queryId: UUID, sql: String, columns: Seq[QueryResult.Col], data: Seq[Seq[Option[Any]]], elapsedMs: Int) = {
    val mappedData = data.map(_.map(_.map(DynamicQuery.transform)))
    QueryResultResponse(resultId, QueryResult(
      queryId = queryId,
      sql = sql,
      columns = columns,
      data = mappedData,
      rowsAffected = mappedData.size
    ), elapsedMs)
  }

  def sendResult(
    result: CachedResult,
    out: Option[ActorRef],
    columns: Seq[QueryResult.Col],
    rowData: Seq[Seq[Option[Any]]],
    elapsedMs: Int,
    moreRowsAvailable: Boolean
  ) = {
    out.foreach { o =>
      val mappedData = rowData.map(_.map(_.map(DynamicQuery.transform)))
      val msg = QueryResultResponse(result.resultId, QueryResult(
        queryId = result.queryId,
        sql = result.sql,
        columns = columns,
        data = mappedData,
        rowsAffected = mappedData.size,
        moreRowsAvailable = moreRowsAvailable,
        source = Some(QueryResult.Source(
          t = "cache",
          name = result.tableName,
          sortable = true
        ))
      ), elapsedMs)
      o ! msg
    }
  }
}
