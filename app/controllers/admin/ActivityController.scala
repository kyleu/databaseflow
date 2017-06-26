package controllers.admin

import java.util.UUID

import controllers.BaseController
import services.audit.AuditRecordService
import services.user.UserSearchService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ActivityController @javax.inject.Inject() (override val ctx: ApplicationContext, userSearchService: UserSearchService) extends BaseController {
  def activity(limit: Int, offset: Int) = withAdminSession("admin-activity") { implicit request =>
    val audits = AuditRecordService.getAll(limit, offset)
    Future.successful(Ok(views.html.admin.systemActivity(request.identity, audits, None, limit, offset, userSearchService)))
  }

  def removeAudit(id: UUID) = withAdminSession("admin-remove-audit") { implicit request =>
    AuditRecordService.removeAudit(id, None)
    Future.successful(Redirect(controllers.admin.routes.ActivityController.activity()).flashing("success" -> messagesApi("activity.remove.confirm", id)(
      request.lang
    )))
  }

  def removeAllAudits() = withAdminSession("admin-remove-audit") { implicit request =>
    AuditRecordService.deleteAll()
    Future.successful(Redirect(controllers.admin.routes.ActivityController.activity()).flashing("success" -> messagesApi("activity.remove.all.confirm")(
      request.lang
    )))
  }
}
