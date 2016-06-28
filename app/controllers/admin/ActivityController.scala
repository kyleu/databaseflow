package controllers.admin

import java.util.UUID

import controllers.BaseController
import services.audit.AuditRecordService
import services.user.UserSearchService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ActivityController @javax.inject.Inject() (override val ctx: ApplicationContext, userSearchService: UserSearchService) extends BaseController {
  def activity = withAdminSession("admin-activity") { implicit request =>
    val audits = AuditRecordService.getFiltered(None)
    Future.successful(Ok(views.html.admin.systemActivity(request.identity, ctx.config.debug, audits, None, AuditRecordService.rowLimit, userSearchService)))
  }

  def removeAudit(id: UUID) = withAdminSession("admin-remove-audit") { implicit request =>
    AuditRecordService.removeAudit(id, None)
    Future.successful(Redirect(controllers.admin.routes.ActivityController.activity()).flashing("success" -> s"Removed activity [$id]."))
  }

  def removeAllAudits() = withAdminSession("admin-remove-audit") { implicit request =>
    AuditRecordService.deleteAll()
    Future.successful(Redirect(controllers.admin.routes.ActivityController.activity()).flashing("success" -> s"Removed all system activity."))
  }
}
