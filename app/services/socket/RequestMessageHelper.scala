package services.socket

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import models._
import models.query.QueryResult.SourceType
import models.query.{RowDataOptions, SavedQuery}
import services.audit.AuditRecordService
import services.database.core.ResultCacheDatabase
import services.query._
import services.result.{CachedResultService, ChartDataService}
import util.Config

trait RequestMessageHelper extends Actor { this: SocketService =>
  override def receive = {
    case mr: MalformedRequest => log.error(s"MalformedRequest:  [${mr.reason}]: [${mr.content}].")

    case p: Ping => out ! Pong(p.timestamp)
    case GetVersion => out ! VersionResponse(Config.projectId, Config.projectName, Config.projectVersion)
    case dr: DebugInfo => handleDebugInfo(dr.data)

    case RefreshSchema => refreshSchema(true)
    case gtd: GetTableDetail => handleGetTableDetail(gtd.name, schema.map(_.enums).getOrElse(Nil))
    case gpd: GetProcedureDetail => handleGetProcedureDetail(gpd.name, schema.map(_.enums).getOrElse(Nil))
    case gvd: GetViewDetail => handleGetViewDetail(gvd.name, schema.map(_.enums).getOrElse(Nil))
    case gcd: GetColumnDetail => handleGetColumnDetail(gcd.owner, gcd.name, gcd.t)

    case BeginTransaction => handleBeginTransaction()
    case RollbackTransaction => handleRollbackTransaction()
    case CommitTransaction => handleCommitTransaction()

    case cq: CheckQuery => QueryCheckService.handleCheckQuery(db, cq.queryId, cq.sql, out)
    case sq: SubmitQuery => handleSubmitQuery(sq.queryId, sq.sql, sq.params, sq.action.getOrElse("run"), sq.resultId)
    case grd: GetRowData => handleGetRowData(grd.key, grd.queryId, grd.name, grd.options, grd.resultId, Some(out))
    case cq: CancelQuery => QueryExecutionService.handleCancelQuery(cq.queryId, cq.resultId, out)
    case cq: CloseQuery => CachedResultService.removeCacheResults(user.id, cq.queryId)

    case cdr: ChartDataRequest => ChartDataService.handleChartDataRequest(cdr, user, connectionId, out, activeTransaction)

    case qsr: QuerySaveRequest => QuerySaveService.handleQuerySaveRequest(user.id, qsr.query, out)
    case qdr: QueryDeleteRequest => QuerySaveService.handleQueryDeleteRequest(user.id, qdr.id, out)

    case srsr: SharedResultSaveRequest => SharedResultService.save(user.id, srsr.result, Some(out))

    case cp: CallProcedure => ProcedureService.callProcedure(connectionId, user.id, cp.queryId, cp.name, cp.params, cp.resultId)
    case rd: RowDelete => RowUpdateService.delete(connectionId, user, rd.name, rd.pk, rd.resultId, out)
    case ru: RowUpdate => RowUpdateService.process(connectionId, user, ru.name, ru.pk, ru.params, ru.resultId, out)

    case gqh: GetQueryHistory => AuditRecordService.handleGetQueryHistory(db.connectionId, user.id, gqh, out)
    case rqh: RemoveAuditHistory => AuditRecordService.handleRemoveAuditHistory(user.id, Some(connectionId), rqh, out)

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
    case ct: SendSocketTrace => handleSocketTrace()
    case ct: SendClientTrace => handleClientTrace()
    case x => throw new IllegalArgumentException(s"Unhandled internal message [${x.getClass.getSimpleName}].")
  }
}
