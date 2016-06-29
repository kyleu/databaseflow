package controllers.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.BaseController
import models.queries.auth.UserQueries
import models.user.{Role, User, UserForms, UserPreferences}
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.database.MasterDatabase
import services.licensing.LicenseService
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
    Future.successful(Ok(views.html.auth.register(UserForms.registrationForm)))
  }

  def register = withoutSession("register") { implicit request =>
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
            val firstUser = MasterDatabase.conn.query(UserQueries.count) == 0
            val roles: Set[Role] = if (LicenseService.isPersonalEdition) {
              Set(Role.User, Role.Admin)
            } else if (firstUser) {
              Set(Role.User, Role.Admin)
            } else {
              Set(Role.User)
            }
            val user = User(
              id = UUID.randomUUID,
              username = Some(data.username),
              preferences = UserPreferences(),
              profile = loginInfo,
              roles = roles
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
              result.flashing("success" -> "You're all set!")
            }
        }
      }
    )
  }
}
