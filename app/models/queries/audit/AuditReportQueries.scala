package models.queries.audit

import java.util.UUID

import models.audit.{AuditRecord, AuditType}
import models.database.{Query, Row}

object AuditReportQueries {
  val tableName = "audit_records"

  case class GetForUser(userId: UUID, limit: Int, offset: Int) extends Query[Seq[AuditRecord]] {
    override val sql = s"""select * from "$tableName" where "owner" = ? order by "occurred" desc limit $limit offset $offset"""
    override val values = Seq(userId)
    override def reduce(rows: Iterator[Row]) = rows.map(AuditRecordQueries.fromRow).toList
  }

  case class GetMatchingQueries(connectionId: UUID, userId: UUID, limit: Int, offset: Int) extends Query[Seq[AuditRecord]] {
    private[this] val typeWhere = s""""audit_type" in ('${AuditType.Query}', '${AuditType.Execute}')"""
    override val sql = {
      s"""select * from "$tableName" where "connection" = ? and "owner" = ? and $typeWhere order by "occurred" desc limit $limit offset $offset"""
    }
    override val values = Seq(connectionId, userId)
    override def reduce(rows: Iterator[Row]) = rows.map(AuditRecordQueries.fromRow).toList
  }
}
