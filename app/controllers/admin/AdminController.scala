package controllers.admin

import controllers.BaseController
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def index = withAdminSession("admin-index") { implicit request =>
    Future.successful(Ok(views.html.admin.index(request.identity, ctx.config.debug)))
  }
}
