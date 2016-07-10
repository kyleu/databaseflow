package controllers.admin

import controllers.BaseSiteController
import play.api.mvc._

import scala.concurrent.Future

abstract class BaseAdminController extends BaseSiteController {
  def withAdminSession(block: (String, Request[AnyContent]) => Future[Result]) = Action.async { implicit request =>
    val id = request.session.get("admin-role")
    id match {
      case Some(username) => block(username, request)
      case None => Future.successful(Redirect("/").withNewSession)
    }
  }
}
