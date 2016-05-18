package models.audit

import java.util.UUID

case class AuditRecord(
  id: UUID,
  auditType: AuditStatus,
  owner: Option[UUID],
  connection: UUID,
  status: AuditStatus,
  attributes: Map[String, String],
  properties: Map[String, Int],
  elapsed: Int,
  occurred: Long
)
