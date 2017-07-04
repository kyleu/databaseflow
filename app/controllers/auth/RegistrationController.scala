package controllers.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.BaseController
import models.queries.auth.UserQueries
import models.settings.SettingKey
import models.user._
import play.api.i18n.Messages
import utils.FutureUtils.defaultContext
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
  def registrationForm(email: Option[String] = None) = withoutSession("form") { implicit request =>
    if (SettingsService.allowRegistration) {
      val form = UserForms.registrationForm.fill(RegistrationData(
        username = email.map(e => if (e.contains('@')) { e.substring(0, e.indexOf('@')) } else { "" }).getOrElse(""),
        email = email.getOrElse("")
      ))
      Future.successful(Ok(views.html.auth.register(request.identity, form)))
    } else {
      Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("error" -> messagesApi("registration.disabled")(request.lang)))
    }
  }

  def register = withoutSession("register") { implicit request =>
    if (!SettingsService.allowRegistration) {
      throw new IllegalStateException(messagesApi("error.cannot.sign.up")(request.lang))
    }
    UserForms.registrationForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.auth.register(request.identity, form))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email.toLowerCase)
        userSearchService.retrieve(loginInfo).flatMap {
          case _ if data.password != data.passwordConfirm => Future.successful(
            Redirect(controllers.auth.routes.RegistrationController.register()).flashing("error" -> messagesApi("registration.passwords.do.not.match")(
              request.lang
            ))
          )
          case Some(_) => Future.successful(
            Redirect(controllers.auth.routes.RegistrationController.register()).flashing("error" -> messagesApi("registration.email.taken")(request.lang))
          )
          case None if !SettingsService.allowSignIn => Future.successful(
            Redirect(controllers.auth.routes.RegistrationController.register()).flashing("error" -> messagesApi("error.sign.in.disabled")(request.lang))
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
              preferences = UserPreferences.empty,
              profile = loginInfo,
              role = role
            )
            val userSaved = userService.save(user)
            val result = request.session.get("returnUrl") match {
              case Some(url) => Redirect(url).withSession(request.session - "returnUrl")
              case None => Redirect(controllers.routes.HomeController.home())
            }
            for {
              _ <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- ctx.silhouette.env.authenticatorService.create(loginInfo)
              value <- ctx.silhouette.env.authenticatorService.init(authenticator)
              result <- ctx.silhouette.env.authenticatorService.embed(value, result)
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
