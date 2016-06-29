package models.queries.audit

import java.sql.Timestamp
import java.util.UUID

import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.database.{Row, Statement}
import models.queries.BaseQueries

object AuditRecordQueries extends BaseQueries[AuditRecord] {
  override protected val tableName = "audit_records"
  override protected val columns = Seq("id", "audit_type", "owner", "connection", "status", "sql", "error", "rows_affected", "elapsed", "occurred")
  override protected val searchColumns = Seq("id", "status", "sql", "error")

  val getAll = GetAll(orderBy = "occurred desc")
  val insert = Insert
  val search = Search
  def removeById(id: UUID) = RemoveById(Seq(id))

  case class Complete(id: UUID, newType: AuditType, rowsAffected: Int, elapsed: Int) extends Statement {
    override def sql = s"update $tableName set audit_type = ?, status = ?, rows_affected = ?, elapsed = ? where id = ?"
    override def values = Seq(newType.toString, AuditStatus.OK.toString, rowsAffected, elapsed, id)
  }
  case class Error(id: UUID, message: String, elapsed: Int) extends Statement {
    override def sql = s"update $tableName set status = ?, error = ?, elapsed = ? where id = ?"
    override def values = Seq(AuditStatus.Error.toString, message, elapsed, id)
  }

  case class RemoveForUser(userId: UUID, connectionId: Option[UUID]) extends Statement {
    override def sql = connectionId match {
      case Some(c) => s"delete from $tableName where owner = ? and connection = ?"
      case None => s"delete from $tableName where owner = ?"
    }
    override def values = connectionId match {
      case Some(c) => Seq(userId, c)
      case None => Seq(userId)
    }
  }
  case class RemoveForAnonymous(connectionId: Option[UUID]) extends Statement {
    override def sql = connectionId match {
      case Some(c) => s"delete from $tableName where owner = is null and connection = ?"
      case None => s"delete from $tableName where owner is null"
    }
    override def values = connectionId match {
      case Some(c) => Seq(c)
      case None => Seq()
    }
  }
  case object RemoveAll extends Statement {
    override def sql = s"truncate $tableName"
  }

  override def fromRow(row: Row) = AuditRecord(
    id = row.as[UUID]("id"),

    auditType = AuditType.withName(row.as[String]("audit_type")),
    owner = row.asOpt[UUID]("owner"),
    connection = row.asOpt[UUID]("connection"),
    status = AuditStatus.withName(row.as[String]("status")),

    sql = row.asOpt[String]("sql"),
    error = row.asOpt[String]("error"),
    rowsAffected = row.asOpt[Int]("rows_affected"),

    elapsed = row.as[Int]("elapsed"),
    occurred = row.as[Timestamp]("occurred").getTime
  )

  override protected def toDataSeq(ar: AuditRecord) = Seq[Any](
    ar.id, ar.auditType.toString, ar.owner, ar.connection, ar.status.toString,
    ar.sql, ar.error, ar.rowsAffected, ar.elapsed, new java.sql.Timestamp(ar.occurred)
  )
}
