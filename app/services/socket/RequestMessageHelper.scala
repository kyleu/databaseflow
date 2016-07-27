package services.socket

import java.util.UUID

import models._
import services.audit.AuditRecordService
import services.data.SampleDatabaseService
import services.query.{PlanExecutionService, QueryCheckService, QueryExecutionService}
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

    case bt: BeginTransaction => timeReceive(bt) { handleBeginTransaction(bt.queryId) }
    case rt: RollbackTransaction => timeReceive(rt) { handleRollbackTransaction(rt.queryId) }
    case ct: CommitTransaction => timeReceive(ct) { handleCommitTransaction(ct.queryId) }

    case cq: CheckQuery => timeReceive(cq) { QueryCheckService.handleCheckQuery(connectionId, cq.queryId, cq.sql, out) }
    case sq: SubmitQuery => timeReceive(sq) { handleSubmitQuery(sq.queryId, sq.sql, sq.action.getOrElse("run"), sq.resultId) }
    case grd: GetRowData => timeReceive(grd) { handleGetRowData(grd.key, grd.queryId, grd.name, grd.options, grd.resultId) }
    case cq: CancelQuery => timeReceive(cq) { QueryExecutionService.handleCancelQuery(cq.queryId, cq.resultId, out) }

    case qsr: QuerySaveRequest => timeReceive(qsr) { QueryExecutionService.handleQuerySaveRequest(user, qsr.query, out) }
    case qdr: QueryDeleteRequest => timeReceive(qdr) { QueryExecutionService.handleQueryDeleteRequest(user, qdr.id, out) }

    case gqh: GetQueryHistory => timeReceive(gqh) { AuditRecordService.handleGetQueryHistory(db.connectionId, user, gqh, out) }
    case rqh: RemoveAuditHistory => timeReceive(rqh) { AuditRecordService.handleRemoveAuditHistory(user.map(_.id), Some(connectionId), rqh, out) }

    case csd: CreateSampleDatabase => timeReceive(csd) { SampleDatabaseService.schedule(db, csd.queryId, out) }

    case im: InternalMessage => handleInternalMessage(im)
    case rm: ResponseMessage => out ! rm
    case x => throw new IllegalArgumentException(s"Unhandled message [${x.getClass.getSimpleName}].")
  }

  private[this] def handleSubmitQuery(queryId: UUID, sql: String, action: String, resultId: UUID) = action match {
    case "run" => QueryExecutionService.handleRunQuery(db, queryId, sql, resultId, connectionId, user.map(_.id), out)
    case "explain" => PlanExecutionService.handleExplainQuery(db, queryId, sql, resultId, out)
    case "analyze" => PlanExecutionService.handleAnalyzeQuery(db, queryId, sql, resultId, out)
    case _ => throw new IllegalArgumentException(action)
  }

  private[this] def handleInternalMessage(im: InternalMessage) = im match {
    case ct: SendSocketTrace => timeReceive(ct) { handleSocketTrace() }
    case ct: SendClientTrace => timeReceive(ct) { handleClientTrace() }
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
