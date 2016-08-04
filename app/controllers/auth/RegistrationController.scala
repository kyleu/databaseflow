package controllers.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.BaseController
import models.queries.auth.UserQueries
import models.settings.SettingKey
import models.user.{Role, User, UserForms, UserPreferences}
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.database.core.MasterDatabase
import services.settings.SettingsService
import services.user.{UserSearchService, UserService}
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class RegistrationController @javax.inject.Inject() (
    override val ctx: ApplicationContext,
    userService: UserService,
    userSearchService: UserSearchService,
    authInfoRepository: AuthInfoRepository,
    hasher: PasswordHasher
) extends BaseController {
  def registrationForm = withoutSession("form") { implicit request =>
    if (SettingsService.allowRegistration) {
      Future.successful(Ok(views.html.auth.register(UserForms.registrationForm)))
    } else {
      Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("error" -> "You cannot sign up at this time. Contact your administrator."))
    }
  }

  def register = withoutSession("register") { implicit request =>
    if (!SettingsService.allowRegistration) {
      throw new IllegalStateException("Unable to sign up at this time. Contact your administrator.")
    }
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.register(form))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userSearchService.retrieve(loginInfo).flatMap {
          case _ if data.password != data.passwordConfirm => Future.successful(
            Redirect(controllers.auth.routes.RegistrationController.register()).flashing("error" -> Messages("registration.passwords.do.not.match"))
          )
          case Some(user) => Future.successful(
            Redirect(controllers.auth.routes.RegistrationController.register()).flashing("error" -> Messages("registration.email.taken"))
          )
          case None =>
            val authInfo = hasher.hash(data.password)
            val firstUser = MasterDatabase.query(UserQueries.count) == 0
            val role: Role = if (firstUser) {
              Role.Admin
            } else {
              Role.withName(SettingsService(SettingKey.DefaultNewUserRole))
            }
            val user = User(
              id = UUID.randomUUID,
              username = data.username,
              preferences = UserPreferences(),
              profile = loginInfo,
              role = role
            )
            val userSaved = userService.save(user)
            for {
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- ctx.silhouette.env.authenticatorService.create(loginInfo)
              value <- ctx.silhouette.env.authenticatorService.init(authenticator)
              result <- ctx.silhouette.env.authenticatorService.embed(value, Redirect(controllers.routes.HomeController.home()))
            } yield {
              ctx.silhouette.env.eventBus.publish(SignUpEvent(userSaved, request))
              ctx.silhouette.env.eventBus.publish(LoginEvent(userSaved, request))
              result.flashing("success" -> "You're all set!")
            }
        }
      }
    )
  }
}
