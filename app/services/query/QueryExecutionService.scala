package services.query

import java.sql.PreparedStatement
import java.util.UUID

import akka.actor.ActorRef
import models._
import models.audit.AuditType
import models.database.Queryable
import models.parse.StatementParser
import models.queries.result.CachedResultQueries
import models.query.{QueryResult, SqlParser}
import models.result.{CachedResult, CachedResultQuery}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.audit.AuditRecordService
import services.database.DatabaseWorkerPool
import services.database.core.MasterDatabase
import services.result.CachedResultService
import utils.{DateUtils, ExceptionUtils, JdbcUtils, Logging}

import scala.concurrent.Future

object QueryExecutionService extends Logging {
  private[this] val activeQueries = collection.mutable.HashMap.empty[UUID, PreparedStatement]

  def handleRunQuery(db: Queryable, queryId: UUID, sql: String, params: Map[String, String], resultId: UUID, connectionId: UUID, owner: UUID, out: ActorRef) = {
    val merged = ParameterService.merge(sql, params)
    val statements = SqlParser.split(merged)
    val first = statements.headOption.getOrElse(throw new IllegalStateException("Missing statement"))._1
    val remaining = statements.tail.map(_._1)
    handleRunStatements(db, queryId, first -> 0, resultId, connectionId, owner, out, remaining)
  }

  def handleRunStatements(
    db: Queryable, queryId: UUID, sql: (String, Int), resultId: UUID, connId: UUID, owner: UUID, out: ActorRef, remaining: Seq[String]
  ): Unit = {
    val startMs = DateUtils.nowMillis
    val auditId = UUID.randomUUID

    def work() = {
      log.info(s"Performing query action [run] with resultId [$resultId] for query [$queryId] with sql [$sql].")
      val startMs = DateUtils.nowMillis
      JdbcUtils.sqlCatch(queryId, sql._1, startMs, resultId, sql._2) { () =>
        val model = CachedResult(resultId, queryId, connId, owner, source = StatementParser.sourceFor(sql._1), sql = sql._1)
        if (sql._2 == 0) {
          MasterDatabase.query(CachedResultQueries.findBy(queryId, owner)).foreach { existing =>
            Future(CachedResultService.remove(existing.resultId))
          }
        }

        Future(AuditRecordService.start(auditId, AuditType.Query, owner, Some(connId), Some(sql._1)))

        val result = db.executeUnknown(CachedResultQuery(sql._2, model, Some(out)), Some(resultId))

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        result match {
          case Left(rm) => rm
          case Right(rowCount) =>
            val qr = QueryResult(queryId = queryId, sql = sql._1, isStatement = true, rowsAffected = rowCount, occurred = startMs)
            QueryResultResponse(resultId, sql._2, qr, durationMs)
        }
      }
    }

    def onSuccess(rm: ResponseMessage) = {
      activeQueries.remove(resultId)
      rm match {
        case qrr: QueryResultResponse =>
          val newType = if (qrr.result.isStatement) { AuditType.Execute } else { AuditType.Query }
          AuditRecordService.complete(auditId, newType, qrr.result.rowsAffected, (DateUtils.nowMillis - startMs).toInt)
        case qrrc: QueryResultRowCount => AuditRecordService.complete(auditId, AuditType.Query, qrrc.count, (DateUtils.nowMillis - startMs).toInt)
        case qer: QueryErrorResponse => AuditRecordService.error(auditId, qer.error.message, (DateUtils.nowMillis - startMs).toInt)
        case _ => throw new IllegalStateException(rm.getClass.getSimpleName)
      }
      out ! rm
      if (remaining.nonEmpty) {
        val next = remaining.headOption.getOrElse(throw new IllegalStateException())
        handleRunStatements(db, queryId, next -> (sql._2 + 1), UUID.randomUUID, connId, owner, out, remaining.tail)
      }
    }

    def onFailure(t: Throwable) = {
      AuditRecordService.error(auditId, t.getMessage, (DateUtils.nowMillis - startMs).toInt)
      ExceptionUtils.actorErrorFunction(out, "StatementError", t)
    }

    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }

  def handleCancelQuery(queryId: UUID, resultId: UUID, out: ActorRef) = {
    activeQueries.get(resultId).foreach(_.cancel())
    out ! QueryCancelledResponse(queryId, resultId)
  }

  def registerRunningStatement(resultId: UUID, stmt: PreparedStatement) = activeQueries(resultId) = stmt
}
