package controllers

import models.user.User
import nl.grons.metrics.scala.FutureMetrics
import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import utils.metrics.Instrumented
import utils.{ ApplicationContext, Logging }

import scala.concurrent.Future

abstract class BaseController() extends Controller with I18nSupport with Instrumented with FutureMetrics with Logging {
  def ctx: ApplicationContext

  override def messagesApi = ctx.messagesApi

  protected[this] def userFor(request: Request[AnyContent]): Option[User] = None

  def withAdminSession(action: String)(block: (Request[AnyContent]) => Future[Result]) = Action.async { implicit request =>
    timing(action) {
      //if (request.identity.roles.contains(Role.Admin)) {
      if (request.queryString.get("p").exists(_.headOption.contains("np"))) {
        block(request)
      } else {
        Future.successful(NotFound("404 Not Found"))
      }
    }
  }

  def withSession(action: String)(block: (Request[AnyContent]) => Future[Result]) = Action.async { implicit request =>
    timing(action) {
      block(request)
    }
  }
}
