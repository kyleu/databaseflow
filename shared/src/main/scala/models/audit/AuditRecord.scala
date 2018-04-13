package models.audit

import java.util.UUID

import util.JsonSerializers._

object AuditRecord {
  implicit val jsonEncoder: Encoder[AuditRecord] = deriveEncoder
  implicit val jsonDecoder: Decoder[AuditRecord] = deriveDecoder
}

case class AuditRecord(
    id: UUID = UUID.randomUUID,
    auditType: AuditType,
    owner: UUID,
    connection: Option[UUID],
    status: AuditStatus = AuditStatus.OK,
    sql: Option[String],
    error: Option[String] = None,
    rowsAffected: Option[Int] = None,
    elapsed: Int,
    occurred: Long
)
