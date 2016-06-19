package services.audit

import java.util.UUID

import models.audit.AuditRecord
import models.queries.audit.AuditRecordQueries
import services.database.MasterDatabase

object AuditService {
  def getAll = MasterDatabase.conn.query(AuditRecordQueries.getAll())

  def insert(auditRecord: AuditRecord) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.insert(auditRecord))
  def delete(id: UUID) = MasterDatabase.conn.executeUpdate(AuditRecordQueries.removeById(id))
}
