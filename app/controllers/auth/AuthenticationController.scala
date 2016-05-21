package controllers.auth

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{ LoginEvent, LogoutEvent }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.BaseController
import models.user.UserForms
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.user.UserSearchService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class AuthenticationController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    userSearchService: UserSearchService,
    credentialsProvider: CredentialsProvider
) extends BaseController {
  def signInForm = withSession("form") { implicit request =>
    Future.successful(Ok(views.html.auth.signin(request.identity, UserForms.signInForm)))
  }

  def authenticateCredentials = withSession("authenticate") { implicit request =>
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.signin(request.identity, form))),
      credentials => {
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val result = Redirect(controllers.routes.HomeController.index())
          userSearchService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              ctx.silhouette.env.authenticatorService.create(loginInfo).flatMap { authenticator =>
                ctx.silhouette.env.eventBus.publish(LoginEvent(user, request))
                ctx.silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  ctx.silhouette.env.authenticatorService.embed(v, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case e: ProviderException =>
            Redirect(controllers.auth.routes.AuthenticationController.signInForm()).flashing("error" -> Messages("authentication.invalid.credentials"))
        }
      }
    )
  }

  def signOut = withSession("signout") { implicit request =>
    val result = Redirect(controllers.routes.HomeController.index())

    request.identity.foreach { user =>
      ctx.silhouette.env.eventBus.publish(LogoutEvent(user, request))
    }
    request.authenticator match {
      case Some(auth) => ctx.silhouette.env.authenticatorService.discard(auth, result)
      case None => Future.successful(result)
    }
  }
}
