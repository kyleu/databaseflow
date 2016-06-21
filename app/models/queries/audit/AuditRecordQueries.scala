package models.queries.audit

import java.sql.Timestamp
import java.util.UUID

import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.database.{Query, Row, Statement}
import models.queries.BaseQueries

object AuditRecordQueries extends BaseQueries[AuditRecord] {

  override protected val tableName = "audit_records"
  override protected val columns = Seq("id", "audit_type", "owner", "connection", "status", "context", "sql", "error", "rows_affected", "elapsed", "occurred")
  override protected val searchColumns = Seq("id", "status", "context", "sql", "error")

  val getAll = GetAll
  val insert = Insert
  val search = Search
  def removeById(id: UUID) = RemoveById(Seq(id))

  case class Complete(id: UUID, rowsAffected: Int, elapsed: Int) extends Statement {
    override def sql = s"update $tableName set status = ?, rowsAffected = ?, elapsed = ? where id = ?"
    override def values = Seq(AuditStatus.OK, rowsAffected, elapsed, id)
  }
  case class Error(id: UUID, message: String, elapsed: Int) extends Statement {
    override def sql = s"update $tableName set status = ?, error = ?, elapsed = ? where id = ?"
    override def values = Seq(AuditStatus.Error, message, elapsed, id)
  }

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
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toList
  }

  override protected def fromRow(row: Row) = AuditRecord(
    id = row.as[UUID]("id"),

    auditType = AuditType.withName(row.as[String]("audit_type")),
    owner = row.asOpt[UUID]("owner"),
    connection = row.as[UUID]("connection"),
    status = AuditStatus.withName(row.as[String]("status")),

    context = row.asOpt[String]("context"),
    sql = row.asOpt[String]("sql"),

    error = row.asOpt[String]("error"),
    rowsAffected = row.asOpt[Int]("rows_affected"),

    elapsed = row.as[Int]("elapsed"),
    occurred = row.as[Timestamp]("occurred").getTime
  )

  override protected def toDataSeq(ar: AuditRecord) = Seq[Any](
    ar.id, ar.auditType.toString, ar.owner, ar.connection, ar.status.toString,
    ar.context, ar.sql, ar.error, ar.rowsAffected, ar.elapsed, new java.sql.Timestamp(ar.occurred)
  )
}
