package controllers

import play.api.i18n.I18nSupport
import util.FutureUtils.defaultContext
import play.api.mvc._
import util.Logging
import util.metrics.Instrumented

import scala.concurrent.Future

abstract class BaseSiteController() extends InjectedController with I18nSupport with Instrumented with Logging {
  def isAdminUser(request: Request[AnyContent]) = request.session.get("admin-role")

  def withAdminSession(action: String)(block: (String, Request[AnyContent]) => Future[Result]) = Action.async(parse.anyContent) { implicit request =>
    val id = isAdminUser(request)
    id match {
      case Some(username) => metrics.timer(action).timeFuture {
        block(username, request)
      }
      case None => Future.successful(Redirect("/").withNewSession)
    }
  }

  def act(action: String)(block: Request[AnyContent] => Future[Result]) = Action.async(parse.anyContent) { implicit request =>
    metrics.timer(action).timeFuture {
      block(request)
    }
  }
}
