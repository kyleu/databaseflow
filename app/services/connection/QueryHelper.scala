package services.connection

import java.util.UUID

import models.engine.EngineQueries
import models.queries.DynamicQuery
import models.query.{ QueryResult, SavedQuery, StatementResult }
import models.schema.{ Table, View }
import models._
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
      val result = db.executeUnknown(DynamicQuery(sql))

      val durationMs = (DateUtils.nowMillis - startMs).toInt
      val msg = result match {
        case Left(rs) => QueryResultResponse(id, QueryResult(
          queryId = queryId,
          title = "Query Results",
          sql = sql,
          columns = rs.cols,
          data = rs.data,
          sortable = false,
          occurred = startMs
        ), durationMs)
        case Right(i) => StatementResultResponse(id, StatementResult(
          queryId = queryId,
          title = "Statement Results",
          sql = sql,
          rowsAffected = i,
          occurred = startMs
        ), durationMs)
      }

      out ! msg
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
    val sql = EngineQueries.selectFrom(table.name, limit = Some(1001))(db.engine)
    log.info(s"Showing data for [${table.name}] using sql [$sql].")
    sqlCatch(queryId, sql, startMs) { () =>
      val result = db.executeUnknown(DynamicQuery(sql))
      val (columns, data) = result match {
        case Left(rs) => rs.cols -> rs.data
        case Right(i) => throw new IllegalStateException(s"Invalid query [$sql] returned statement result.")
      }

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
    val sql = EngineQueries.selectFrom(view.name, limit = Some(1001))(db.engine)
    log.info(s"Showing data for [${view.name}] using sql [$sql].")
    sqlCatch(queryId, sql, startMs) { () =>
      val result = db.executeUnknown(DynamicQuery(sql))

      val (columns, data) = result match {
        case Left(rs) => rs.cols -> rs.data
        case Right(i) => throw new IllegalStateException(s"Invalid query [$sql] returned statement result.")
      }
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
