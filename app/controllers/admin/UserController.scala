package controllers.admin

import controllers.BaseController
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class UserController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def users = withSession("admin-users") { implicit request =>
    Future.successful(Ok(views.html.admin.users(request.identity, ctx.config.debug, userService.getAll)))
  }
}
