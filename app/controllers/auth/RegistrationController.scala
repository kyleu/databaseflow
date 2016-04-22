package controllers.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{ LoginEvent, LoginInfo, SignUpEvent }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.BaseController
import models.user.{ User, UserForms, UserPreferences }
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.user.{ UserSearchService, UserService }
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class RegistrationController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    userService: UserService,
    userSearchService: UserSearchService,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    hasher: PasswordHasher
) extends BaseController {
  def registrationForm = withSession("form") { implicit request =>
    Future.successful(Ok(views.html.auth.register(request.identity, UserForms.registrationForm)))
  }

  def register = withSession("register") { implicit request =>
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.register(request.identity, form))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userSearchService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(Ok(views.html.auth.register(request.identity, UserForms.registrationForm.fill(data), Some(Messages("registration.email.taken")))))
          case None =>
            val authInfo = hasher.hash(data.password)
            val user = User(
              id = UUID.randomUUID,
              username = Some(data.username),
              profile = loginInfo,
              preferences = UserPreferences()
            )
            val userSaved = userService.save(user)
            for {
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- ctx.silhouette.env.authenticatorService.create(loginInfo)
              value <- ctx.silhouette.env.authenticatorService.init(authenticator)
              result <- ctx.silhouette.env.authenticatorService.embed(value, Redirect(controllers.routes.HomeController.index()))
            } yield {
              ctx.silhouette.env.eventBus.publish(SignUpEvent(userSaved, request))
              ctx.silhouette.env.eventBus.publish(LoginEvent(userSaved, request))
              result
            }
        }
      }
    )
  }
}
