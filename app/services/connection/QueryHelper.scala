package services.connection

import java.util.UUID

import models._
import models.queries.DynamicQuery
import models.query.{ QueryResult, SavedQuery, StatementResult }
import services.database.DatabaseWorkerPool
import services.query.SavedQueryService
import utils.{ DateUtils, ExceptionUtils, Logging }

import scala.util.control.NonFatal

trait QueryHelper extends Logging { this: ConnectionService =>
  protected[this] def handleQuerySaveRequest(sq: SavedQuery) = {
    log.info(s"Saving query as [${sq.id}].")
    try {
      val result = SavedQueryService.save(sq, user.map(_.id))
      out ! QuerySaveResponse(savedQuery = result)
    } catch {
      case NonFatal(x) => out ! QuerySaveResponse(error = Some(x.getMessage), savedQuery = sq)
    }
  }

  protected[this] def handleQueryDeleteRequest(id: UUID) = {
    log.info(s"Deleting query [$id].")
    try {
      val result = SavedQueryService.delete(id, user.map(_.id))
      out ! QueryDeleteResponse(id = id)
    } catch {
      case NonFatal(x) => out ! QueryDeleteResponse(id = id, error = Some(x.getMessage))
    }
  }

  protected[this] def handleRunQuery(queryId: UUID, sql: String) = {
    def work() = {
      log.info(s"Performing query action [run] for sql [$sql].")
      val id = UUID.randomUUID
      val startMs = DateUtils.nowMillis
      sqlCatch(queryId, sql, startMs) { () =>
        val result = db.executeUnknown(DynamicQuery(sql))

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        result match {
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
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "PlanError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
