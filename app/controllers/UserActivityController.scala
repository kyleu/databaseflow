package controllers

import java.util.UUID

import models.settings.SettingKey
import services.audit.AuditRecordService
import services.settings.SettingsService
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class UserActivityController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def activity = withSession("activity") { implicit request =>
    val audits = AuditRecordService.getForUser(request.identity.map(_.id))
    val removeCall = if (SettingsService.asBool(SettingKey.AllowAuditRemoval)) {
      Some(controllers.routes.UserActivityController.removeAudit _)
    } else {
      None
    }
    Future.successful(Ok(views.html.profile.userActivity(request.identity, ctx.config.debug, audits, None, AuditRecordService.rowLimit, removeCall)))
  }

  def removeAudit(id: UUID) = withSession("remove-audit") { implicit request =>
    AuditRecordService.removeAudit(id, None)
    Future.successful(Redirect(controllers.routes.UserActivityController.activity()).flashing("success" -> s"Removed activity [$id]."))
  }

  def removeAllAudits() = withSession("remove-audit") { implicit request =>
    request.identity match {
      case Some(u) => AuditRecordService.deleteAllForUser(u.id, None)
      case None => AuditRecordService.deleteAllForGuest(None)
    }
    Future.successful(Redirect(controllers.routes.UserActivityController.activity()).flashing("success" -> "Removed all user activity."))
  }
}
