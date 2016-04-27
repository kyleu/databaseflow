package controllers

import com.mohiva.play.silhouette.api.actions.{ SecuredRequest, UserAwareRequest }
import models.auth.AuthEnv
import models.user.Role
import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import utils.metrics.Instrumented
import utils.{ ApplicationContext, Logging }

import scala.concurrent.Future

abstract class BaseController() extends Controller with I18nSupport with Instrumented with Logging {
  def ctx: ApplicationContext

  override def messagesApi = ctx.messagesApi

  def withAdminSession(action: String)(block: (SecuredRequest[AuthEnv, AnyContent]) => Future[Result]) = {
    ctx.silhouette.SecuredAction.async { implicit request =>
      metrics.timer(action).timeFuture {
        if (request.identity.roles.contains(Role.Admin)) {
          block(request)
        } else {
          Future.successful(NotFound("404 Not Found"))
        }
      }
    }
  }

  def withSession(action: String)(block: (UserAwareRequest[AuthEnv, AnyContent]) => Future[Result]) = ctx.silhouette.UserAwareAction.async { implicit request =>
    metrics.timer(action).timeFuture {
      block(request)
    }
  }
}
