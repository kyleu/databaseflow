package models.audit

import java.util.UUID

case class AuditRecord(
  id: UUID = UUID.randomUUID,
  auditType: AuditType,
  owner: Option[UUID],
  connection: Option[UUID],
  status: AuditStatus = AuditStatus.OK,
  sql: Option[String],
  error: Option[String] = None,
  rowsAffected: Option[Int] = None,
  elapsed: Int,
  occurred: Long
)
