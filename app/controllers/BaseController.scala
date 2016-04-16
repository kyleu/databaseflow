package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.user.{ Role, User }
import nl.grons.metrics.scala.FutureMetrics
import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ AnyContent, Result }
import utils.metrics.Instrumented
import utils.{ ApplicationContext, Logging }

import scala.concurrent.Future

abstract class BaseController() extends Silhouette[User, CookieAuthenticator] with I18nSupport with Instrumented with FutureMetrics with Logging {
  def ctx: ApplicationContext

  override def messagesApi = ctx.messagesApi
  override def env = ctx.authEnv

  def withAdminSession(action: String)(block: (SecuredRequest[AnyContent]) => Future[Result]) = SecuredAction.async { implicit request =>
    timing(action) {
      if (request.identity.roles.contains(Role.Admin)) {
        block(request)
      } else {
        Future.successful(NotFound("404 Not Found"))
      }
    }
  }

  def withSession(action: String)(block: (UserAwareRequest[AnyContent]) => Future[Result]) = UserAwareAction.async { implicit request =>
    timing(action) {
      block(request)
    }
  }
}
