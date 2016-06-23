package services.audit

import java.util.UUID

import akka.actor.ActorRef
import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.queries.audit.AuditRecordQueries
import models.user.User
import models.{AuditRecordRemoved, AuditRecordResponse, GetAuditHistory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.database.MasterDatabase
import utils.DateUtils

import scala.concurrent.Future

object AuditRecordService {
  def getAll = MasterDatabase.conn.query(AuditRecordQueries.getAll())

  def handleGetAuditHistory(connectionId: UUID, user: Option[User], gqh: GetAuditHistory, out: ActorRef) = {
    val matching = MasterDatabase.conn.query(AuditRecordQueries.GetMatching(connectionId, user.map(_.id), gqh.limit, gqh.offset))
    out ! AuditRecordResponse(matching)
  }

  def removeAudit(id: UUID, out: ActorRef) = if (delete(id) == 1) {
    out ! AuditRecordRemoved(id)
  }

  def start(
    auditId: UUID,
    t: AuditType,
    owner: Option[UUID] = None,
    connection: Option[UUID] = None,
    context: Option[String] = None,
    sql: Option[String] = None
  ) = insert(AuditRecord(
    id = auditId,
    auditType = t,
    owner = owner,
    connection = connection,
    status = AuditStatus.Started,
    context = context,
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

  def create(t: AuditType, owner: Option[UUID], connection: Option[UUID], context: Option[String] = None, sql: Option[String] = None, elapsed: Int = 0) = {
    insert(AuditRecord(
      id = UUID.randomUUID,
      auditType = t,
      owner = owner,
      connection = connection,
      status = AuditStatus.OK,
      context = context,
      sql = sql,
      error = None,
      rowsAffected = None,
      elapsed = elapsed,
      occurred = DateUtils.nowMillis
    ))
  }

  def insert(auditRecord: AuditRecord): Future[Unit] = Future {
    MasterDatabase.conn.executeUpdate(AuditRecordQueries.insert(auditRecord))
  }

  def delete(id: UUID) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.removeById(id))
}
