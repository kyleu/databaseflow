package services.socket

import java.util.UUID

import models._
import models.engine.EngineQueries
import models.queries.DynamicQuery
import models.query.{QueryResult, RowDataOptions}
import models.schema.ForeignKey
import services.database.DatabaseWorkerPool
import services.database.core.ResultCacheDatabase
import services.schema.SchemaService
import utils.{DateUtils, ExceptionUtils, JdbcUtils, Logging}

trait DataHelper extends Logging { this: SocketService =>
  protected[this] def handleGetRowData(key: String, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = key match {
    case "table" => handleGetTableRowData(queryId, name, options, resultId)
    case "view" => handleGetViewRowData(queryId, name, options, resultId)
    case "cache" => handleGetCacheRowData(queryId, name, options, resultId)
  }

  private[this] def handleGetTableRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getTable(connectionId, name) match {
      case Some(table) => handleShowDataResponse(queryId, "table", table.name, table.foreignKeys, options, resultId, cacheDb = false)
      case None =>
        log.warn(s"Attempted to show data for invalid table [$name].")
        out ! ServerError("Invalid Table", s"[$name] is not a valid table.")
    }
  }

  private[this] def handleGetViewRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    SchemaService.getView(connectionId, name) match {
      case Some(view) => handleShowDataResponse(queryId, "view", view.name, Nil, options, resultId, cacheDb = false)
      case None =>
        log.warn(s"Attempted to show data for invalid view [$name].")
        out ! ServerError("Invalid View", s"[$name] is not a valid view.")
    }
  }

  private[this] def handleGetCacheRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) = {
    handleShowDataResponse(queryId, "cache", name, Nil, options, resultId, cacheDb = true)
  }

  private[this] def handleShowDataResponse(
    queryId: UUID, t: String, name: String, keys: Seq[ForeignKey], options: RowDataOptions, resultId: UUID, cacheDb: Boolean
  ) {
    def work() = {
      val startMs = DateUtils.nowMillis
      val optionsNewLimit = options.copy(limit = options.limit.map(_ + 1))
      val (database, engine) = if (cacheDb) {
        ResultCacheDatabase.conn -> ResultCacheDatabase.conn.engine
      } else {
        activeTransaction.getOrElse(db) -> db.engine
      }
      val sql = EngineQueries.selectFrom(name, optionsNewLimit)(engine)
      log.info(s"Showing data for [$name] using sql [$sql].")
      JdbcUtils.sqlCatch(queryId, sql, startMs, resultId, 0) { () =>
        val result = database.query(DynamicQuery(sql))

        val (trimmedData, moreRowsAvailable) = options.limit match {
          case Some(limit) if result.data.size > limit => result.data.take(limit) -> true
          case _ => result.data -> false
        }
        val columnsWithRelations = result.cols.map { col =>
          keys.find(_.references.exists(_.source == col.name)) match {
            case Some(fk) => col.copy(
              relationTable = Some(fk.targetTable),
              relationColumn = fk.references.find(_.source == col.name).map(_.target)
            )
            case None => col
          }
        }

        val qr = QueryResult(
          queryId = queryId,
          sql = sql,
          columns = columnsWithRelations,
          data = trimmedData,
          rowsAffected = trimmedData.length,
          moreRowsAvailable = moreRowsAvailable,
          source = Some(options.toSource(t, name)),
          occurred = startMs
        )

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        QueryResultResponse(resultId, 0, qr, durationMs)
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "ShowDataError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
