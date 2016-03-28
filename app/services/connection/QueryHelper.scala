package services.connection

import java.util.UUID

import models.{ QueryResultResponse, ServerError }
import models.queries.DynamicQuery
import models.query.QueryResult
import services.database.MasterDatabase
import utils.{ DateUtils, Logging }

trait QueryHelper extends Logging { this: ConnectionService =>
  def attemptConnect() = MasterDatabase.databaseFor(connectionId) match {
    case Right(d) =>
      Some(d)
    case Left(x) =>
      log.warn("Error attempting to connect to database.", x)
      out ! ServerError("Database Connect Failed", x.getMessage)
      None
  }

  def handleRunQuery(queryId: UUID, sql: String) = {
    log.info(s"Performing query action [run] for sql [$sql].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    sqlCatch(queryId, sql, startMs) { () =>
      val result = db.query(DynamicQuery(sql))
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = "Query Results",
        sql = sql,
        columns = result._1,
        data = result._2,
        sortable = false,
        occurred = startMs
      ), durationMs)
    }
  }

  def handleShowTableData(queryId: UUID, name: String) = schema.tables.find(_ == name) match {
    case Some(table) => handleShowTableDataResponse(queryId, name)
    case None => schema.views.find(_ == name) match {
      case Some(table) => handleShowTableDataResponse(queryId, name)
      case None =>
        log.warn(s"Attempted to view invalid table or view [$name].")
        out ! ServerError("Invalid Table", s"[$name] is not a valid table or view.")
    }
  }

  private[this] def handleShowTableDataResponse(queryId: UUID, name: String) {
    log.info(s"Showing table [$name].")
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    val sql = s"select * from $name limit 1001"
    sqlCatch(queryId, sql, startMs) { () =>
      val result = db.query(DynamicQuery(sql))
      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = name,
        sql = sql,
        columns = result._1,
        data = result._2,
        sortable = true,
        occurred = startMs
      ), durationMs)
    }
  }
}
