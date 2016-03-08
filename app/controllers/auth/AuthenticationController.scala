package controllers.auth

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{ LoginEvent, LogoutEvent }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import controllers.BaseController
import models.user.UserForms
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class AuthenticationController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def signInForm = withSession("form") { implicit request =>
    Future.successful(Ok(views.html.auth.signin(request.identity, UserForms.signInForm)))
  }

  def authenticateCredentials = withSession("authenticate") { implicit request =>
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.signin(request.identity, form))),
      credentials => env.credentials.authenticate(credentials).flatMap { loginInfo =>
        val result = Redirect(controllers.routes.HomeController.index())
        env.identityService.retrieve(loginInfo).flatMap {
          case Some(user) => env.authenticatorService.create(loginInfo).flatMap { authenticator =>
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            env.authenticatorService.init(authenticator).flatMap(v => env.authenticatorService.embed(v, result))
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user."))
        }
      }.recover {
        case e: ProviderException =>
          Redirect(controllers.auth.routes.AuthenticationController.signInForm()).flashing("error" -> Messages("authentication.invalid.credentials"))
      }
    )
  }

  def signOut = withSession("signout") { implicit request =>
    val result = Redirect(controllers.routes.HomeController.index())
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, result).map(x => result)
  }
}
