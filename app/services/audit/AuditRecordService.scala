package services.audit

import java.util.UUID

import akka.actor.ActorRef
import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.queries.audit.AuditRecordQueries
import models.user.User
import models.{AuditRecordRemoved, AuditRecordResponse, GetAuditHistory}
import services.database.MasterDatabase
import utils.DateUtils

object AuditRecordService {
  def getAll = MasterDatabase.conn.query(AuditRecordQueries.getAll())

  def handleGetAuditHistory(connectionId: UUID, user: Option[User], gqh: GetAuditHistory, out: ActorRef) = {
    val matching = MasterDatabase.conn.query(AuditRecordQueries.GetMatching(connectionId, user.map(_.id), gqh.limit, gqh.offset))
    out ! AuditRecordResponse(matching)
  }

  def removeAudit(user: Option[User], id: UUID, out: ActorRef) = {
    val success = delete(id) == 1
    if (success) {
      out ! AuditRecordRemoved(id)
    }
  }

  def start(auditId: UUID, t: AuditType, owner: Option[UUID], connection: UUID, context: Option[String], sql: Option[String]) = insert(AuditRecord(
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

  def complete(auditId: UUID, rowsAffected: Int, elapsed: Int) = {
    MasterDatabase.conn.executeUpdate(AuditRecordQueries.Complete(auditId, rowsAffected, elapsed))
  }
  def error(auditId: UUID, message: String, elapsed: Int) = {
    MasterDatabase.conn.executeUpdate(AuditRecordQueries.Error(auditId, message, elapsed))
  }

  def insert(t: AuditType, owner: Option[UUID], connection: UUID, context: Option[String], sql: Option[String], elapsed: Int): Int = insert(AuditRecord(
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
  def insert(auditRecord: AuditRecord): Int = MasterDatabase.conn.executeUpdate(AuditRecordQueries.insert(auditRecord))

  def delete(id: UUID) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.removeById(id))
}
