package services.query

import java.sql.PreparedStatement
import java.util.UUID

import akka.actor.ActorRef
import models._
import models.audit.AuditType
import models.query.{QueryResult, SavedQuery}
import models.result.{CachedResult, CachedResultQuery}
import models.user.User
import services.audit.AuditRecordService
import services.database.{DatabaseConnection, DatabaseWorkerPool}
import utils.{DateUtils, ExceptionUtils, JdbcUtils, Logging}

import scala.util.control.NonFatal

object QueryExecutionService extends Logging {
  private[this] val activeQueries = collection.mutable.HashMap.empty[UUID, PreparedStatement]

  def handleQuerySaveRequest(user: Option[User], sq: SavedQuery, out: ActorRef) = {
    log.info(s"Saving query as [${sq.id}].")
    try {
      val result = SavedQueryService.save(sq, user.map(_.id))
      AuditRecordService.create(AuditType.SaveQuery, user.map(_.id), None, Some(result.id.toString))
      out ! QuerySaveResponse(savedQuery = result)
    } catch {
      case NonFatal(x) => out ! QuerySaveResponse(error = Some(x.getMessage), savedQuery = sq)
    }
  }

  def handleQueryDeleteRequest(user: Option[User], id: UUID, out: ActorRef) = {
    log.info(s"Deleting query [$id].")
    try {
      SavedQueryService.delete(id, user.map(_.id))
      AuditRecordService.create(AuditType.DeleteQuery, user.map(_.id), None, Some(id.toString))
      out ! QueryDeleteResponse(id = id)
    } catch {
      case NonFatal(x) => out ! QueryDeleteResponse(id = id, error = Some(x.getMessage))
    }
  }

  def handleRunQuery(db: DatabaseConnection, queryId: UUID, sql: String, resultId: UUID, connectionId: UUID, owner: Option[UUID], out: ActorRef) = {
    val startMs = DateUtils.nowMillis
    val auditId = UUID.randomUUID

    def work() = {
      log.info(s"Performing query action [run] with resultId [$resultId] for query [$queryId] with sql [$sql].")
      val startMs = DateUtils.nowMillis
      JdbcUtils.sqlCatch(queryId, sql, startMs, resultId) { () =>
        val model = CachedResult(resultId, queryId, connectionId, owner, sql = sql)
        AuditRecordService.start(auditId, AuditType.Query, owner, Some(connectionId), None, Some(sql))
        val result = db.executeUnknown(CachedResultQuery(model, Some(out)), Some(resultId))

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        result match {
          case Left(rowCount) => rowCount
          case Right(i) => QueryResultResponse(resultId, QueryResult(
            queryId = queryId,
            sql = sql,
            isStatement = true,
            rowsAffected = i,
            occurred = startMs
          ), durationMs)
        }
      }
    }

    def onSuccess(rm: ResponseMessage) = {
      activeQueries.remove(resultId)
      val (rowCount, isStatement) = rm match {
        case m: QueryResultResponse => m.result.rowsAffected -> m.result.isStatement
        case m: QueryResultRowCount => m.count -> false
        case _ => throw new IllegalStateException(rm.getClass.getSimpleName)
      }
      val newType = if (isStatement) { AuditType.Execute } else { AuditType.Query }
      AuditRecordService.complete(auditId, newType, rowCount, (DateUtils.nowMillis - startMs).toInt)
      out ! rm
    }

    def onFailure(t: Throwable) = {
      AuditRecordService.error(auditId, t.getMessage, (DateUtils.nowMillis - startMs).toInt)
      ExceptionUtils.actorErrorFunction(out, "PlanError", t)
    }

    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  def handleCancelQuery(queryId: UUID, resultId: UUID, out: ActorRef) = {
    activeQueries.get(resultId).foreach(_.cancel())
    out ! QueryCancelledResponse(queryId, resultId)
  }

  def registerRunningStatement(resultId: UUID, stmt: PreparedStatement) = activeQueries(resultId) = stmt
}
