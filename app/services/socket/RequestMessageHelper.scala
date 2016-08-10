package services.socket

import java.util.UUID

import models._
import services.audit.AuditRecordService
import services.data.SampleDatabaseService
import services.query.{PlanExecutionService, QueryCheckService, QueryExecutionService, QuerySaveService}
import services.result.CachedResultService
import utils.Config
import utils.metrics.InstrumentedActor

trait RequestMessageHelper extends InstrumentedActor { this: SocketService =>
  override def receiveRequest = {
    case mr: MalformedRequest => timeReceive(mr) { log.error(s"MalformedRequest:  [${mr.reason}]: [${mr.content}].") }

    case p: Ping => timeReceive(p) { out ! Pong(p.timestamp) }
    case GetVersion => timeReceive(GetVersion) { out ! VersionResponse(Config.version) }
    case dr: DebugInfo => timeReceive(dr) { handleDebugInfo(dr.data) }

    case RefreshSchema => timeReceive(RefreshSchema) { refreshSchema(true) }
    case gtd: GetTableDetail => timeReceive(gtd) { handleGetTableDetail(gtd.name) }
    case gvd: GetViewDetail => timeReceive(gvd) { handleGetViewDetail(gvd.name) }
    case gpd: GetProcedureDetail => timeReceive(gpd) { handleGetProcedureDetail(gpd.name) }

    case BeginTransaction => timeReceive(BeginTransaction) { handleBeginTransaction() }
    case RollbackTransaction => timeReceive(RollbackTransaction) { handleRollbackTransaction() }
    case CommitTransaction => timeReceive(CommitTransaction) { handleCommitTransaction() }

    case cq: CheckQuery => timeReceive(cq) { QueryCheckService.handleCheckQuery(db, cq.queryId, cq.sql, out) }
    case sq: SubmitQuery => timeReceive(sq) { handleSubmitQuery(sq.queryId, sq.sql, sq.action.getOrElse("run"), sq.resultId) }
    case grd: GetRowData => timeReceive(grd) { handleGetRowData(grd.key, grd.queryId, grd.name, grd.options, grd.resultId) }
    case cq: CancelQuery => timeReceive(cq) { QueryExecutionService.handleCancelQuery(cq.queryId, cq.resultId, out) }
    case cq: CloseQuery => timeReceive(cq) { CachedResultService.removeCacheResults(user.id, cq.queryId) }

    case qsr: QuerySaveRequest => timeReceive(qsr) { QuerySaveService.handleQuerySaveRequest(user.id, qsr.query, out) }
    case qdr: QueryDeleteRequest => timeReceive(qdr) { QuerySaveService.handleQueryDeleteRequest(user.id, qdr.id, out) }

    case gqh: GetQueryHistory => timeReceive(gqh) { AuditRecordService.handleGetQueryHistory(db.connectionId, user.id, gqh, out) }
    case rqh: RemoveAuditHistory => timeReceive(rqh) { AuditRecordService.handleRemoveAuditHistory(user.id, Some(connectionId), rqh, out) }

    case csd: CreateSampleDatabase => timeReceive(csd) { SampleDatabaseService.schedule(db, csd.queryId, out) }

    case im: InternalMessage => handleInternalMessage(im)
    case rm: ResponseMessage => out ! rm
    case x => throw new IllegalArgumentException(s"Unhandled message [${x.getClass.getSimpleName}].")
  }

  private[this] def handleSubmitQuery(queryId: UUID, sql: String, action: String, resultId: UUID) = action match {
    case "run" => QueryExecutionService.handleRunQuery(activeTransaction.getOrElse(db), queryId, sql, resultId, connectionId, user.id, out)
    case "explain" => PlanExecutionService.handleExplainQuery(activeTransaction.getOrElse(db), db.engine, queryId, sql, resultId, out)
    case "analyze" => PlanExecutionService.handleAnalyzeQuery(activeTransaction.getOrElse(db), db.engine, queryId, sql, resultId, out)
    case _ => throw new IllegalArgumentException(action)
  }

  private[this] def handleInternalMessage(im: InternalMessage) = im match {
    case ct: SendSocketTrace => timeReceive(ct) { handleSocketTrace() }
    case ct: SendClientTrace => timeReceive(ct) { handleClientTrace() }
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
