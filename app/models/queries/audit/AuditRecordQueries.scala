package models.queries.audit

import java.util.UUID

import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.database.Row
import models.queries.BaseQueries

object AuditRecordQueries extends BaseQueries[AuditRecord] {
  override protected val tableName = "audit_records"
  override protected val columns = Seq("id", "audit_type", "owner", "connection", "status", "elapsed", "occurred")
  override protected val searchColumns = Seq("id")

  val insert = Insert
  val search = Search

  override protected def fromRow(row: Row) = {
    AuditRecord(
      id = row.as[UUID]("id"),

      auditType = AuditType.withName(row.as[String]("audit_type")),
      owner = row.asOpt[UUID]("owner"),
      connection = row.as[UUID]("connection"),
      status = AuditStatus.withName(row.as[String]("status")),
      elapsed = row.as[Int]("elapsed"),
      occurred = row.as[Long]("occurred")
    )
  }

  override protected def toDataSeq(ar: AuditRecord) = {
    Seq[Any](ar.id, ar.auditType.toString, ar.owner, ar.connection, ar.status.toString, ar.elapsed)
  }
}
