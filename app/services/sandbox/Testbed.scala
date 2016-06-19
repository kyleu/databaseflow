package services.sandbox

import java.util.UUID

import com.fasterxml.jackson.databind.ser.std.UUIDSerializer
import models.audit.{AuditRecord, AuditStatus, AuditType}
import services.audit.AuditRecordService
import utils.{ApplicationContext, DateUtils}

import scala.concurrent.Future

object Testbed extends SandboxTask {
  override def id = "testbed"
  override def name = "Testbed"
  override def description = ""

  def randomRecord() = AuditRecord(
    id = UUID.randomUUID,
    auditType = AuditType.SignIn,
    owner = None,
    connection = UUID.randomUUID,
    status = AuditStatus.OK,
    context = Some("ctx"),
    sql = Some("sql"),
    elapsed = 10,
    occurred = 1000
  )

  override def run(ctx: ApplicationContext) = {
    AuditRecordService.insert(randomRecord())

    val ret = "Hello!"
    Future.successful(ret)
  }
}
