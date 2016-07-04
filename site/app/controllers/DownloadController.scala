package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

@javax.inject.Singleton
class DownloadController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def personalEdition() = Action.async { implicit request =>
    Future.successful(Ok("Personal Edition!"))
  }

  def teamEdition() = Action.async { implicit request =>
    Future.successful(Ok("Team Edition!"))
  }
}
