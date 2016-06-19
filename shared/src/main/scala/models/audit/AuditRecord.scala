package models.audit

import java.util.UUID

case class AuditRecord(
  id: UUID,
  auditType: AuditType,
  owner: Option[UUID],
  connection: UUID,
  status: AuditStatus,
  context: Option[String],
  sql: Option[String],
  elapsed: Int,
  occurred: Long
)
