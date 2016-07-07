package services.socket

import java.util.UUID

import akka.actor.{ActorRef, Props}
import models._
import models.audit.AuditType
import models.query.SavedQuery
import models.schema.Schema
import models.user.User
import services.audit.AuditRecordService
import services.data.SampleDatabaseService
import services.database.MasterDatabase
import services.query.{PlanExecutionService, QueryCheckService, QueryExecutionService}
import utils.metrics.InstrumentedActor
import utils.{Config, Logging}

object SocketService {
  def props(id: Option[UUID], supervisor: ActorRef, connectionId: UUID, user: Option[User], out: ActorRef, sourceAddress: String) = {
    Props(new SocketService(id.getOrElse(UUID.randomUUID), supervisor, connectionId, user, out, sourceAddress))
  }
}

class SocketService(
    val id: UUID = UUID.randomUUID,
    val supervisor: ActorRef,
    val connectionId: UUID,
    val user: Option[User],
    val out: ActorRef,
    val sourceAddress: String
) extends InstrumentedActor with StartHelper with DataHelper with DetailHelper with Logging {

  protected[this] var currentUsername = user.flatMap(_.username)
  protected[this] var userPreferences = user.map(_.preferences)
  protected[this] var dbOpt = attemptConnect()
  protected[this] val db = dbOpt.getOrElse(throw new IllegalStateException("Cannot connect to database."))

  protected[this] var schema: Option[Schema] = None
  protected[this] var savedQueries: Option[Seq[SavedQuery]] = None

  protected[this] var pendingDebugChannel: Option[ActorRef] = None

  AuditRecordService.create(AuditType.Connect, user.map(_.id), Some(connectionId))

  override def preStart() = onStart()

  override def receiveRequest = {
    // Incoming basic messages
    case mr: MalformedRequest => timeReceive(mr) { log.error(s"MalformedRequest:  [${mr.reason}]: [${mr.content}].") }

    case p: Ping => timeReceive(p) { out ! Pong(p.timestamp) }
    case GetVersion => timeReceive(GetVersion) { out ! VersionResponse(Config.version) }
    case dr: DebugInfo => timeReceive(dr) { handleDebugInfo(dr.data) }

    case RefreshSchema => timeReceive(RefreshSchema) { refreshSchema(true) }
    case gtd: GetTableDetail => timeReceive(gtd) { handleGetTableDetail(gtd.name) }
    case gvd: GetViewDetail => timeReceive(gvd) { handleGetViewDetail(gvd.name) }
    case gpd: GetProcedureDetail => timeReceive(gpd) { handleGetProcedureDetail(gpd.name) }

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

  override def postStop() = {
    if (MasterDatabase.isOpen) {
      AuditRecordService.create(AuditType.Disconnect, user.map(_.id), Some(connectionId))
    }
    supervisor ! SocketStopped(id)
  }

  protected[this] def handleSubmitQuery(queryId: UUID, sql: String, action: String, resultId: UUID) = action match {
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
