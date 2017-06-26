package controllers.user

import java.util.UUID

import controllers.BaseController
import models.settings.SettingKey
import services.audit.AuditRecordService
import services.settings.SettingsService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class UserActivityController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def activity(limit: Int, offset: Int) = withSession("activity") { implicit request =>
    val audits = AuditRecordService.getForUser(request.identity.id, limit, offset)
    val removeCall = if (SettingsService.asBool(SettingKey.AllowAuditRemoval)) {
      Some(controllers.user.routes.UserActivityController.removeAudit _)
    } else {
      None
    }
    Future.successful(Ok(views.html.profile.userActivity(request.identity, audits, None, limit, offset, removeCall)))
  }

  def removeAudit(id: UUID) = withSession("remove-audit") { implicit request =>
    AuditRecordService.removeAudit(id, None)
    Future.successful(Redirect(controllers.user.routes.UserActivityController.activity()).flashing("success" -> messagesApi("activity.remove.ok", id)(
      request.lang
    )))
  }

  def removeAllAudits() = withSession("remove-audit") { implicit request =>
    AuditRecordService.deleteAllForUser(request.identity.id, None)
    Future.successful(Redirect(controllers.user.routes.UserActivityController.activity()).flashing(
      "success" -> messagesApi("activity.remove.all.user.confirm")(request.lang)
    ))
  }
}
