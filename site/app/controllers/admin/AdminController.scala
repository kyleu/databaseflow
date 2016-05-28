package controllers.admin

import play.api.i18n.MessagesApi
import play.api.mvc.Action

import scala.concurrent.Future

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseAdminController {
  def index() = Action.async { implicit request =>
    Future.successful(Ok(views.html.admin.index()))
  }
}
