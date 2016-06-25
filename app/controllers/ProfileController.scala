package controllers

import java.util.UUID

import models.settings.SettingKey
import models.user.UserForms
import services.audit.AuditRecordService
import services.settings.SettingsService
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ProfileController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def view() = withSession("view") { implicit request =>
    Future.successful(Ok(views.html.profile.view(request.identity, debug = false)))
  }

  def save() = withSession("view") { implicit request =>
    val user = request.identity.getOrElse(throw new IllegalStateException("Not logged in."))
    UserForms.profileForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.profile.view(request.identity, debug = false))),
      profileData => {
        val newPrefs = user.preferences.copy(
          theme = profileData.theme
        )
        val newUser = user.copy(username = Some(profileData.username), preferences = newPrefs)
        userService.save(newUser, update = true)
        Future.successful(Redirect(controllers.routes.HomeController.index()))
      }
    )
  }

  def activity() = withSession("activity") { implicit request =>
    val audits = AuditRecordService.getFiltered(request.identity.map(_.id))
    val removeCall = if (SettingsService(SettingKey.AllowAuditRemoval) == "true") {
      Some(controllers.routes.ProfileController.removeAudit _)
    } else {
      None
    }
    Future.successful(Ok(views.html.profile.userActivity(request.identity, ctx.config.debug, audits, None, AuditRecordService.rowLimit, removeCall)))
  }

  def removeAudit(id: UUID) = withSession("remove-audit") { implicit request =>
    AuditRecordService.removeAudit(id, None)
    Future.successful(Redirect(controllers.routes.ProfileController.activity()).flashing("success" -> s"Removed activity [$id]."))
  }
}
