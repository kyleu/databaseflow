package controllers.admin

import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._

import scala.concurrent.Future

abstract class BaseAdminController extends Controller with I18nSupport {
  override def messagesApi: MessagesApi

  def withSession(action: String)(block: (Request[AnyContent]) => Future[Result]) = Action.async { implicit request =>
    block(request)
  }
}
