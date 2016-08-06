package controllers

import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import utils.Logging
import utils.metrics.Instrumented

import scala.concurrent.Future

abstract class BaseSiteController() extends Controller with I18nSupport with Instrumented with Logging {
  def isAdminUser(request: Request[AnyContent]) = request.session.get("admin-role")

  def withAdminSession(action: String)(block: (String, Request[AnyContent]) => Future[Result]) = Action.async { implicit request =>
    val id = isAdminUser(request)
    id match {
      case Some(username) => metrics.timer(action).timeFuture {
        block(username, request)
      }
      case None => Future.successful(Redirect("/").withNewSession)
    }
  }

  def act(action: String)(block: Request[AnyContent] => Future[Result]) = Action.async { implicit request =>
    metrics.timer(action).timeFuture {
      block(request)
    }
  }
}
