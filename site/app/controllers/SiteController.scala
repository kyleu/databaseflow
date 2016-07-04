package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

object SiteController {
  val cors = Seq(
    "Access-Control-Allow-Headers" -> "Content-Type,x-requested-with,Authorization,Access-Control-Allow-Origin",
    "Access-Control-Allow-Methods" -> "GET,POST,OPTIONS",
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Max-Age" -> "360"
  )
}

@javax.inject.Singleton
class SiteController @javax.inject.Inject() (implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def splash() = Action.async { implicit request =>
    Future.successful(Ok(views.html.splash()).withHeaders(SiteController.cors: _*))
  }

  def index() = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()).withHeaders(SiteController.cors: _*))
  }
}
