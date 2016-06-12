package models.audit

import java.util.UUID

case class AuditRecord(
  id: UUID,
  auditType: AuditType,
  owner: Option[UUID],
  connection: UUID,
  status: AuditStatus,
  elapsed: Int,
  occurred: Long
)
