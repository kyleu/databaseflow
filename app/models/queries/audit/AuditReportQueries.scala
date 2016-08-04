package models.queries.audit

import java.util.UUID

import models.audit.{AuditRecord, AuditType}
import models.database.{Query, Row}

object AuditReportQueries {
  val tableName = "audit_records"

  case class GetForUser(userId: Option[UUID], limit: Int, offset: Int) extends Query[Seq[AuditRecord]] {
    private[this] val whereClause = userId match {
      case Some(uid) => "owner = ?"
      case None => "owner is null"
    }
    override val sql = s"select * from $tableName where $whereClause limit $limit offset $offset"
    override def values = userId match {
      case Some(uid) => Seq(uid)
      case None => Seq.empty
    }
    override def reduce(rows: Iterator[Row]) = rows.map(AuditRecordQueries.fromRow).toList
  }

  case class GetMatchingQueries(connectionId: UUID, userId: Option[UUID], limit: Int, offset: Int) extends Query[Seq[AuditRecord]] {
    private[this] val whereClause = userId match {
      case Some(uid) => " and \"owner\" = ?"
      case None => " and \"owner\" is null"
    }
    private[this] val typeWhere = s"audit_type in ('${AuditType.Query}', '${AuditType.Execute}')"
    override def sql: String = s"""select * from "$tableName" where "connection" = ?$whereClause and $typeWhere limit $limit offset $offset"""
    override def values = userId match {
      case Some(uid) => Seq(connectionId, uid)
      case None => Seq(connectionId)
    }
    override def reduce(rows: Iterator[Row]) = rows.map(AuditRecordQueries.fromRow).toList
  }
}
