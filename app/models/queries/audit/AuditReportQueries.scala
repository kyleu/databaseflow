package models.queries.audit

import java.util.UUID

import models.audit.{AuditRecord, AuditType}
import models.database.{Query, Row}

object AuditReportQueries {
  val tableName = "audit_records"

  case class GetForUser(userId: UUID, limit: Int, offset: Int) extends Query[Seq[AuditRecord]] {
    private[this] val whereClause = "owner = ?"
    override val sql = s"select * from $tableName where $whereClause limit $limit offset $offset"
    override def values = Seq(userId)
    override def reduce(rows: Iterator[Row]) = rows.map(AuditRecordQueries.fromRow).toList
  }

  case class GetMatchingQueries(connectionId: UUID, userId: UUID, limit: Int, offset: Int) extends Query[Seq[AuditRecord]] {
    private[this] val whereClause = " and \"owner\" = ?"
    private[this] val typeWhere = s"audit_type in ('${AuditType.Query}', '${AuditType.Execute}')"
    override def sql: String = s"""select * from "$tableName" where "connection" = ?$whereClause and $typeWhere limit $limit offset $offset"""
    override def values = Seq(connectionId, userId)
    override def reduce(rows: Iterator[Row]) = rows.map(AuditRecordQueries.fromRow).toList
  }
}
