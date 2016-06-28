package services.audit

import java.util.UUID

import akka.actor.ActorRef
import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.queries.audit.AuditRecordQueries
import models.user.User
import models.{AuditRecordRemoved, AuditRecordResponse, GetQueryHistory, RemoveAuditHistory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.database.MasterDatabase
import utils.{DateUtils, Logging}

import scala.concurrent.Future
import scala.util.control.NonFatal

object AuditRecordService extends Logging {
  val rowLimit = 100

  def getAll = MasterDatabase.conn.query(AuditRecordQueries.getAll)
  def getFiltered(userId: Option[UUID]) = getAll

  def handleGetQueryHistory(connectionId: UUID, user: Option[User], gqh: GetQueryHistory, out: ActorRef) = {
    val matching = MasterDatabase.conn.query(AuditRecordQueries.GetMatchingQueries(connectionId, user.map(_.id), gqh.limit, gqh.offset))
    out ! AuditRecordResponse(matching)
  }

  def handleRemoveAuditHistory(userId: Option[UUID], connectionId: Option[UUID], rqh: RemoveAuditHistory, out: ActorRef) = rqh.id match {
    case Some(auditId) => AuditRecordService.removeAudit(auditId, Some(out))
    case None => userId match {
      case Some(id) => AuditRecordService.removeAuditsForUser(id, connectionId, Some(out))
      case None => AuditRecordService.removeAuditsForGuest(connectionId, Some(out))
    }
  }

  def removeAudit(id: UUID, out: Option[ActorRef]) = if (delete(id) == 1) {
    out.foreach(_ ! AuditRecordRemoved(Some(id)))
  }

  def removeAuditsForUser(userId: UUID, connectionId: Option[UUID], out: Option[ActorRef]) = if (deleteAllForUser(userId, connectionId) > 1) {
    out.foreach(_ ! AuditRecordRemoved(None))
  }

  def removeAuditsForGuest(connectionId: Option[UUID], out: Option[ActorRef]) = if (deleteAllForGuest(connectionId) > 1) {
    out.foreach(_ ! AuditRecordRemoved(None))
  }

  def start(
    auditId: UUID,
    t: AuditType,
    owner: Option[UUID] = None,
    connection: Option[UUID] = None,
    sql: Option[String] = None
  ) = insert(AuditRecord(
    id = auditId,
    auditType = t,
    owner = owner,
    connection = connection,
    status = AuditStatus.Started,
    sql = sql,
    error = None,
    rowsAffected = None,
    elapsed = 0,
    occurred = DateUtils.nowMillis
  ))

  def complete(auditId: UUID, newType: AuditType, rowsAffected: Int, elapsed: Int) = {
    MasterDatabase.conn.executeUpdate(AuditRecordQueries.Complete(auditId, newType, rowsAffected, elapsed))
  }
  def error(auditId: UUID, message: String, elapsed: Int) = {
    MasterDatabase.conn.executeUpdate(AuditRecordQueries.Error(auditId, message, elapsed))
  }

  def create(t: AuditType, owner: Option[UUID], connection: Option[UUID], sql: Option[String] = None, elapsed: Int = 0) = {
    insert(AuditRecord(
      id = UUID.randomUUID,
      auditType = t,
      owner = owner,
      connection = connection,
      status = AuditStatus.OK,
      sql = sql,
      error = None,
      rowsAffected = None,
      elapsed = elapsed,
      occurred = DateUtils.nowMillis
    ))
  }

  def insert(auditRecord: AuditRecord) = Future {
    MasterDatabase.conn.executeUpdate(AuditRecordQueries.insert(auditRecord))
  }.onFailure {
    case NonFatal(x) => log.warn(s"Unable to log audit [$auditRecord].", x)
  }

  def delete(id: UUID) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.removeById(id))
  def deleteAllForUser(userId: UUID, connectionId: Option[UUID]) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.RemoveForUser(userId, connectionId))
  def deleteAllForGuest(connectionId: Option[UUID]) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.RemoveForAnonymous(connectionId))
  def deleteAll() = MasterDatabase.conn.executeUpdate(AuditRecordQueries.RemoveAll)
}
