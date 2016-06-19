package models.queries.audit

import java.util.UUID

import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.database.{Query, Row}
import models.queries.BaseQueries

object AuditRecordQueries extends BaseQueries[AuditRecord] {
  override protected val tableName = "audit_records"
  override protected val columns = Seq("id", "audit_type", "owner", "connection", "status", "context", "sql", "elapsed", "occurred")
  override protected val searchColumns = Seq("id")

  val getAll = GetAll
  val insert = Insert
  val search = Search
  def removeById(id: UUID) = RemoveById(Seq(id))

  case class GetMatching(connectionId: UUID, userId: Option[UUID], limit: Int, offset: Int) extends Query[Seq[AuditRecord]] {
    private[this] val whereClause = userId match {
      case Some(uid) => " and \"owner\" = ?"
      case None => " and \"owner\" is null"
    }
    override def sql: String = s"""select * from "$tableName" where "connection" = ?$whereClause limit $limit offset $offset"""
    override def values = userId match {
      case Some(uid) => Seq(connectionId, uid)
      case None => Seq(connectionId)
    }
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toSeq
  }

  override protected def fromRow(row: Row) = AuditRecord(
    id = row.as[UUID]("id"),

    auditType = AuditType.withName(row.as[String]("audit_type")),
    owner = row.asOpt[UUID]("owner"),
    connection = row.as[UUID]("connection"),
    status = AuditStatus.withName(row.as[String]("status")),

    context = row.asOpt[String]("context"),
    sql = row.asOpt[String]("sql"),

    elapsed = row.as[Int]("elapsed"),
    occurred = row.as[Long]("occurred")
  )

  override protected def toDataSeq(ar: AuditRecord) = {
    Seq[Any](ar.id, ar.auditType.toString, ar.owner, ar.connection, ar.status.toString, ar.context, ar.sql, ar.elapsed)
  }
}
