package controllers

import models.user.UserForms
import services.user.UserService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class ProfileController @javax.inject.Inject() (override val ctx: ApplicationContext, userService: UserService) extends BaseController {
  def view() = withSession("view") { implicit request =>
    Future.successful(Ok(views.html.profile(request.identity, debug = false)))
  }

  def save() = withSession("view") { implicit request =>
    val user = request.identity.getOrElse(throw new IllegalStateException("Not logged in."))
    UserForms.profileForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.profile(request.identity, debug = false))),
      profileData => {
        val newPrefs = user.preferences.copy(
          theme = profileData.theme
        )
        val newUser = user.copy(username = Some(profileData.username), preferences = newPrefs)
        userService.save(newUser, update = true)
        Future.successful(Redirect(routes.HomeController.index()))
      }
    )
  }
}
