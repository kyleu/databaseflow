package services.socket

import java.util.UUID

import akka.actor.ActorRef
import models._
import models.query.QueryResult.SourceType
import models.query.{RowDataOptions, SavedQuery}
import services.audit.AuditRecordService
import services.database.core.ResultCacheDatabase
import services.query._
import services.result.{CachedResultService, ChartDataService}
import util.Config
import util.metrics.InstrumentedActor

trait RequestMessageHelper extends InstrumentedActor { this: SocketService =>
  override def receiveRequest = {
    case mr: MalformedRequest => timeReceive(mr) { log.error(s"MalformedRequest:  [${mr.reason}]: [${mr.content}].") }

    case p: Ping => timeReceive(p) { out ! Pong(p.timestamp) }
    case GetVersion => timeReceive(GetVersion) { out ! VersionResponse(Config.projectId, Config.projectName, Config.projectVersion) }
    case dr: DebugInfo => timeReceive(dr) { handleDebugInfo(dr.data) }

    case RefreshSchema => timeReceive(RefreshSchema) { refreshSchema(true) }
    case gtd: GetTableDetail => timeReceive(gtd) { handleGetTableDetail(gtd.name, schema.map(_.enums).getOrElse(Nil)) }
    case gpd: GetProcedureDetail => timeReceive(gpd) { handleGetProcedureDetail(gpd.name, schema.map(_.enums).getOrElse(Nil)) }
    case gvd: GetViewDetail => timeReceive(gvd) { handleGetViewDetail(gvd.name, schema.map(_.enums).getOrElse(Nil)) }
    case gcd: GetColumnDetail => timeReceive(gcd) { handleGetColumnDetail(gcd.owner, gcd.name, gcd.t) }

    case BeginTransaction => timeReceive(BeginTransaction) { handleBeginTransaction() }
    case RollbackTransaction => timeReceive(RollbackTransaction) { handleRollbackTransaction() }
    case CommitTransaction => timeReceive(CommitTransaction) { handleCommitTransaction() }

    case cq: CheckQuery => timeReceive(cq) { QueryCheckService.handleCheckQuery(db, cq.queryId, cq.sql, out) }
    case sq: SubmitQuery => timeReceive(sq) { handleSubmitQuery(sq.queryId, sq.sql, sq.params, sq.action.getOrElse("run"), sq.resultId) }
    case grd: GetRowData => timeReceive(grd) { handleGetRowData(grd.key, grd.queryId, grd.name, grd.options, grd.resultId, Some(out)) }
    case cq: CancelQuery => timeReceive(cq) { QueryExecutionService.handleCancelQuery(cq.queryId, cq.resultId, out) }
    case cq: CloseQuery => timeReceive(cq) { CachedResultService.removeCacheResults(user.id, cq.queryId) }

    case cdr: ChartDataRequest => timeReceive(cdr) { ChartDataService.handleChartDataRequest(cdr, user, connectionId, out, activeTransaction) }

    case qsr: QuerySaveRequest => timeReceive(qsr) { QuerySaveService.handleQuerySaveRequest(user.id, qsr.query, out) }
    case qdr: QueryDeleteRequest => timeReceive(qdr) { QuerySaveService.handleQueryDeleteRequest(user.id, qdr.id, out) }

    case srsr: SharedResultSaveRequest => timeReceive(srsr) { SharedResultService.save(user.id, srsr.result, Some(out)) }

    case cp: CallProcedure => timeReceive(cp) { ProcedureService.callProcedure(connectionId, user.id, cp.queryId, cp.name, cp.params, cp.resultId) }
    case rd: RowDelete => timeReceive(rd) { RowUpdateService.delete(connectionId, user, rd.name, rd.pk, rd.resultId, out) }
    case ru: RowUpdate => timeReceive(ru) { RowUpdateService.process(connectionId, user, ru.name, ru.pk, ru.params, ru.resultId, out) }

    case gqh: GetQueryHistory => timeReceive(gqh) { AuditRecordService.handleGetQueryHistory(db.connectionId, user.id, gqh, out) }
    case rqh: RemoveAuditHistory => timeReceive(rqh) { AuditRecordService.handleRemoveAuditHistory(user.id, Some(connectionId), rqh, out) }

    case im: InternalMessage => handleInternalMessage(im)
    case rm: ResponseMessage => out ! rm
    case x => throw new IllegalArgumentException(s"Unhandled request message [${x.getClass.getSimpleName}].")
  }

  private[this] def handleSubmitQuery(queryId: UUID, sql: String, params: Seq[SavedQuery.Param], action: String, resultId: UUID) = action match {
    case "run" => QueryExecutionService.handleRunQuery(activeTransaction.getOrElse(db), queryId, sql, params, resultId, connectionId, user.id, out)
    case "explain" => PlanExecutionService.handleExplainQuery(activeTransaction.getOrElse(db), db.engine, queryId, sql, params, resultId, out)
    case "analyze" => PlanExecutionService.handleAnalyzeQuery(activeTransaction.getOrElse(db), db.engine, queryId, sql, params, resultId, out)
    case _ => throw new IllegalArgumentException(action)
  }

  private[this] def handleGetRowData(key: SourceType, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID, someRef: Some[ActorRef]) = {
    val dbAccess = key match {
      case SourceType.Cache => (ResultCacheDatabase.connectionId, ResultCacheDatabase.conn, ResultCacheDatabase.conn.engine)
      case _ => (connectionId, activeTransaction.getOrElse(db), db.engine)
    }
    RowDataService.handleGetRowData(dbAccess, key, RowDataService.Config(queryId, name, Nil, options, resultId, Some(out)))
  }

  private[this] def handleInternalMessage(im: InternalMessage) = im match {
    case ct: SendSocketTrace => timeReceive(ct) { handleSocketTrace() }
    case ct: SendClientTrace => timeReceive(ct) { handleClientTrace() }
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
