package services.sandbox

import java.util.UUID

import models.audit.{AuditRecord, AuditStatus, AuditType}
import models.user.User
import services.audit.AuditRecordService
import utils.ApplicationContext

import scala.concurrent.Future

object Testbed extends SandboxTask {
  override def id = "testbed"
  override def name = "Testbed"
  override def description = ""

  def randomRecord() = AuditRecord(
    auditType = AuditType.SignIn,
    owner = User.mock.id,
    connection = Some(UUID.randomUUID),
    status = AuditStatus.OK,
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
