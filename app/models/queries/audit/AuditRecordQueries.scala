package models.queries.audit

import java.util.UUID

import models.audit.{ AuditRecord, AuditStatus, AuditType }
import models.database.Row
import models.queries.BaseQueries

object AuditRecordQueries extends BaseQueries[AuditRecord] {
  override protected val tableName = "audit_records"
  override protected val columns = Seq("id", "audit_type", "owner", "connection", "status", "attributes", "properties", "elapsed", "occurred")
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
      attributes = row.as[String]("attributes").split("::").map { s =>
        val split = s.split("/")
        split(0) -> split(1)
      }.toMap,
      properties = row.as[String]("properties").split("::").map { s =>
        val split = s.split("/")
        split(0) -> split(1).toInt
      }.toMap,
      elapsed = row.as[Int]("elapsed"),
      occurred = row.as[Long]("occurred")
    )
  }

  override protected def toDataSeq(ar: AuditRecord) = {
    val attributes = ar.attributes.map(x => x._1 + "/" + x._2).mkString("::")
    val properties = ar.properties.map(x => x._1 + "/" + x._2).mkString("::")
    Seq[Any](ar.id, ar.auditType.toString, ar.owner, ar.connection, ar.status.toString, attributes, properties, ar.elapsed)
  }
}
