package services.connection

import java.util.UUID

import models._
import models.engine.EngineQueries
import models.queries.DynamicQuery
import models.query.{ QueryResult, RowDataOptions }
import models.schema.ForeignKey
import services.database.DatabaseWorkerPool
import services.schema.SchemaService
import utils.{ DateUtils, ExceptionUtils, Logging }

trait DataHelper extends Logging { this: ConnectionService =>
  protected[this] def handleGetTableRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getTable(connectionId, name) match {
      case Some(table) => handleShowDataResponse(queryId, "table", table.name, table.foreignKeys, options, resultId)
      case None =>
        log.warn(s"Attempted to show data for invalid table [$name].")
        out ! ServerError("Invalid Table", s"[$name] is not a valid table.")
    }
  }

  protected[this] def handleGetViewRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getView(connectionId, name) match {
      case Some(view) => handleShowDataResponse(queryId, "view", view.name, Nil, options, resultId)
      case None =>
        log.warn(s"Attempted to show data for invalid view [$name].")
        out ! ServerError("Invalid Table", s"[$name] is not a valid view.")
    }
  }

  private[this] def handleShowDataResponse(queryId: UUID, t: String, name: String, foreignKeys: Seq[ForeignKey], options: RowDataOptions, resultId: UUID) {
    def work() = {
      val startMs = DateUtils.nowMillis
      val optionsNewLimit = options.copy(limit = options.limit.map(_ + 1))
      val sql = EngineQueries.selectFrom(name, optionsNewLimit)(db.engine)
      log.info(s"Showing data for [$name] using sql [$sql].")
      sqlCatch(queryId, sql, startMs, resultId) { () =>
        val result = db.executeUnknown(DynamicQuery(sql))
        val (columns, data) = result match {
          case Left(rs) => rs.cols -> rs.data
          case Right(i) => throw new IllegalStateException(s"Invalid query [$sql] returned statement result.")
        }

        val (trimmedData, moreRowsAvailable) = options.limit match {
          case Some(limit) => if (data.size > limit) {
            data.take(limit) -> true
          } else {
            data -> false
          }
          case None => data -> false
        }
        val columnsWithRelations = columns.map { col =>
          foreignKeys.find(_.references.exists(_.source == col.name)) match {
            case Some(fk) => col.copy(
              relationTable = Some(fk.targetTable),
              relationColumn = fk.references.find(_.source == col.name).map(_.target)
            )
            case None => col
          }
        }

        //log.info(s"Query result: [$result].")
        val durationMs = (DateUtils.nowMillis - startMs).toInt
        QueryResultResponse(resultId, Seq(QueryResult(
          queryId = queryId,
          title = name + " Data",
          sql = sql,
          columns = columnsWithRelations,
          data = trimmedData,
          rowsAffected = trimmedData.length,
          moreRowsAvailable = moreRowsAvailable,
          source = Some(QueryResult.Source(
            t = t,
            name = name,
            sortable = true,
            sortedColumn = options.orderByCol,
            sortedAscending = options.orderByAsc,
            dataOffset = options.offset.getOrElse(0)
          )),
          occurred = startMs
        )), durationMs)
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "ShowDataError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
