package controllers

import services.settings.SettingsService
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    settingsService: SettingsService,
    userService: UserService
) extends BaseController {

  def settings() = withSession("admin-settings") { implicit request =>
    Future.successful(Ok(views.html.admin.settings(request.identity, ctx.config.debug, settingsService.getAll)))
  }

  def users() = withSession("admin-users") { implicit request =>
    Future.successful(Ok(views.html.admin.users(request.identity, ctx.config.debug, userService.getAll)))
  }
}
