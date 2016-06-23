package models.audit

import java.util.UUID

case class AuditRecord(
  id: UUID,
  auditType: AuditType,
  owner: Option[UUID],
  connection: Option[UUID],
  status: AuditStatus,
  context: Option[String],
  sql: Option[String],
  error: Option[String],
  rowsAffected: Option[Int],
  elapsed: Int,
  occurred: Long
)
