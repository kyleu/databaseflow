package services.audit

import java.util.UUID

import akka.actor.ActorRef
import models.audit.AuditRecord
import models.queries.audit.AuditRecordQueries
import models.user.User
import models.{AuditRecordRemoved, AuditRecordResponse, GetAuditHistory}
import services.database.MasterDatabase

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

  def insert(auditRecord: AuditRecord) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.insert(auditRecord))
  def delete(id: UUID) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.removeById(id))
}
