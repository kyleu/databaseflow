package services.connection

import java.util.UUID

import models._
import models.engine.EngineQueries
import models.queries.DynamicQuery
import models.query.{ QueryResult, RowDataOptions }
import models.schema.{ Table, View }
import services.database.DatabaseWorkerPool
import services.schema.SchemaService
import utils.{ DateUtils, ExceptionUtils, Logging }

trait DataHelper extends Logging { this: ConnectionService =>
  protected[this] def handleGetTableRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getTable(connectionId, name) match {
      case Some(table) => handleShowTableDataResponse(queryId, table, options, resultId)
      case None =>
        log.warn(s"Attempted to show data for invalid table [$name].")
        out ! ServerError("Invalid Table", s"[$name] is not a valid table.")
    }
  }

  protected[this] def handleGetViewRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getView(connectionId, name) match {
      case Some(view) => handleShowViewDataResponse(queryId, view, options, resultId)
      case None =>
        log.warn(s"Attempted to show data for invalid view [$name].")
        out ! ServerError("Invalid Table", s"[$name] is not a valid view.")
    }
  }

  private[this] def handleShowTableDataResponse(queryId: UUID, table: Table, options: RowDataOptions, resultId: UUID) {
    def work() = {
      val startMs = DateUtils.nowMillis
      val sql = EngineQueries.selectFrom(table.name, options)(db.engine)
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
        QueryResultResponse(resultId, QueryResult(
          queryId = queryId,
          title = table.name,
          sql = sql,
          columns = columnsWithRelations,
          data = data,
          sortable = true,
          dataOffset = options.offset.getOrElse(0),
          occurred = startMs
        ), durationMs)
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "TableDataError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  private[this] def handleShowViewDataResponse(queryId: UUID, view: View, options: RowDataOptions, resultId: UUID) {
    def work() = {
      val startMs = DateUtils.nowMillis
      val sql = EngineQueries.selectFrom(view.name, options)(db.engine)
      log.info(s"Showing data for [${view.name}] using sql [$sql].")
      sqlCatch(queryId, sql, startMs) { () =>
        val result = db.executeUnknown(DynamicQuery(sql))

        val (columns, data) = result match {
          case Left(rs) => rs.cols -> rs.data
          case Right(i) => throw new IllegalStateException(s"Invalid query [$sql] returned statement result.")
        }
        //log.info(s"Query result: [$result].")
        val durationMs = (DateUtils.nowMillis - startMs).toInt
        QueryResultResponse(resultId, QueryResult(
          queryId = queryId,
          title = view.name,
          sql = sql,
          columns = columns,
          data = data,
          sortable = true,
          dataOffset = options.offset.getOrElse(0),
          occurred = startMs
        ), durationMs)
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "ViewDataError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
