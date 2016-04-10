package services.connection

import java.util.UUID

import models.engine.rdbms.Oracle
import models.queries.DynamicQuery
import models.query.{ QueryResult, SavedQuery }
import models.schema.{ Table, View }
import models.{ QueryDeleteResponse, QueryResultResponse, QuerySaveResponse, ServerError }
import services.database.MasterDatabase
import services.query.SavedQueryService
import services.schema.SchemaService
import utils.{ DateUtils, Logging }

import scala.util.control.NonFatal

trait QueryHelper extends Logging { this: ConnectionService =>
  def attemptConnect() = MasterDatabase.databaseFor(connectionId) match {
    case Right(d) =>
      Some(d)
    case Left(x) =>
      log.warn("Error attempting to connect to database.", x)
      out ! ServerError("Database Connect Failed", x.getMessage)
      None
  }

  protected[this] def handleQuerySaveRequest(sq: SavedQuery) = {
    log.info(s"Saving query as [${sq.id}].")
    try {
      val result = SavedQueryService.save(sq, Some(user.id))
      out ! QuerySaveResponse(savedQuery = result)
    } catch {
      case NonFatal(x) => out ! QuerySaveResponse(error = Some(x.getMessage), savedQuery = sq)
    }
  }

  protected[this] def handleQueryDeleteRequest(id: UUID) = {
    log.info(s"Deleting query [$id].")
    try {
      val result = SavedQueryService.delete(id, Some(user.id))
      out ! QueryDeleteResponse(id = id)
    } catch {
      case NonFatal(x) => out ! QueryDeleteResponse(id = id, error = Some(x.getMessage))
    }
  }

  protected[this] def handleRunQuery(queryId: UUID, sql: String) = {
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

  protected[this] def handleGetTableRowData(queryId: UUID, name: String) = SchemaService.getTable(connectionId, name) match {
    case Some(table) => handleShowTableDataResponse(queryId, table)
    case None =>
      log.warn(s"Attempted to show data for invalid table [$name].")
      out ! ServerError("Invalid Table", s"[$name] is not a valid table.")
  }

  protected[this] def handleGetViewRowData(queryId: UUID, name: String) = SchemaService.getView(connectionId, name) match {
    case Some(view) => handleShowViewDataResponse(queryId, view)
    case None =>
      log.warn(s"Attempted to show data for invalid view [$name].")
      out ! ServerError("Invalid Table", s"[$name] is not a valid view.")
  }

  private[this] def handleShowTableDataResponse(queryId: UUID, table: Table) {
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    val qi = db.engine.quoteIdentifier
    val sql = db.engine match {
      case Oracle => s"""select * from $qi${table.name}$qi where rownum <= 1001"""
      case _ => s"""select * from $qi${table.name}$qi limit 1001"""
    }
    log.info(s"Showing data for [${table.name}] using sql [$sql].")
    sqlCatch(queryId, sql, startMs) { () =>
      val (columns, data) = db.query(DynamicQuery(sql))
      val columnsWithRelations = columns.map { col =>
        table.foreignKeys.find(_.references.exists(_.source == col.name)) match {
          case Some(fk) => col.copy(
            relationTable = Some(fk.targetTable),
            relationColumn = fk.references.find(_.source == col.name).map(_.target)
          )
          case None => col
        }
      }

      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = table.name,
        sql = sql,
        columns = columnsWithRelations,
        data = data,
        sortable = true,
        occurred = startMs
      ), durationMs)
    }
  }

  private[this] def handleShowViewDataResponse(queryId: UUID, view: View) {
    val id = UUID.randomUUID
    val startMs = DateUtils.nowMillis
    val sql = s"""select * from ${db.engine.quoteIdentifier}${view.name}${db.engine.quoteIdentifier} limit 1001"""
    log.info(s"Showing data for [${view.name}] using sql [$sql].")
    sqlCatch(queryId, sql, startMs) { () =>
      val (columns, data) = db.query(DynamicQuery(sql))

      //log.info(s"Query result: [$result].")
      val durationMs = (DateUtils.nowMillis - startMs).toInt
      out ! QueryResultResponse(id, QueryResult(
        queryId = queryId,
        title = view.name,
        sql = sql,
        columns = columns,
        data = data,
        sortable = true,
        occurred = startMs
      ), durationMs)
    }
  }
}
