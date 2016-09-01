package controllers.auth

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.BaseController
import models.audit.AuditType
import models.user.UserForms
import play.api.i18n.{Lang, Messages}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.audit.AuditRecordService
import services.user.UserSearchService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class AuthenticationController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    userSearchService: UserSearchService,
    credentialsProvider: CredentialsProvider
) extends BaseController {
  def signInForm = withoutSession("form") { implicit request =>
    Future.successful(Ok(views.html.auth.signin(request.identity, UserForms.signInForm)))
  }

  def authenticateCredentials = withoutSession("authenticate") { implicit request =>
    UserForms.signInForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.signin(request.identity, form))),
      credentials => {
        val creds = credentials.copy(identifier = credentials.identifier.toLowerCase)
        credentialsProvider.authenticate(creds).flatMap { loginInfo =>
          val result = Redirect(controllers.routes.HomeController.home())
          userSearchService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              ctx.silhouette.env.authenticatorService.create(loginInfo).flatMap { authenticator =>
                ctx.silhouette.env.eventBus.publish(LoginEvent(user, request))
                ctx.silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  AuditRecordService.create(AuditType.SignIn, user.id, None)
                  ctx.silhouette.env.authenticatorService.embed(v, result.withLang(Lang(user.preferences.language.code)))
                }
              }
            case None => Future.failed(new IdentityNotFoundException(messagesApi("error.missing.user", loginInfo.providerID)))
          }
        }.recover {
          case e: ProviderException =>
            Redirect(controllers.auth.routes.AuthenticationController.signInForm()).flashing("error" -> Messages("authentication.invalid.credentials"))
        }
      }
    )
  }

  def signOut = withSession("signout") { implicit request =>
    implicit val result = Redirect(controllers.routes.HomeController.home())

    AuditRecordService.create(AuditType.SignOut, request.identity.id, None)
    ctx.silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    ctx.silhouette.env.authenticatorService.discard(request.authenticator, result.clearingLang)
  }
}
