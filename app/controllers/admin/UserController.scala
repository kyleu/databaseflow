package controllers.admin

import java.util.UUID

import controllers.BaseController
import services.user.{UserSearchService, UserService}
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class UserController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    userService: UserService,
    userSearchService: UserSearchService
) extends BaseController {
  def users = withSession("admin-users") { implicit request =>
    Future.successful(Ok(views.html.admin.users(request.identity, ctx.config.debug, userService.getAll)))
  }

  def view(id: UUID) = withSession("admin-users") { implicit request =>
    val user = userSearchService.retrieve(id).getOrElse(throw new IllegalStateException(s"Invalid user [$id]."))
    Future.successful(Ok(views.html.admin.view(request.identity, ctx.config.debug, user)))
  }
}
