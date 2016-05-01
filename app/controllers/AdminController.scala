package controllers

import services.settings.SettingsService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (override val ctx: ApplicationContext, settingsService: SettingsService) extends BaseController {
  def index() = withSession("admin-index") { implicit request =>
    Future.successful(Ok(views.html.admin.index(request.identity, ctx.config.debug, settingsService.getAll)))
  }
}
