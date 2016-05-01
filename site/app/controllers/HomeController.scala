package controllers

import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, Controller }

import scala.concurrent.Future

@javax.inject.Singleton
class HomeController @javax.inject.Inject() (implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def index() = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }
}
