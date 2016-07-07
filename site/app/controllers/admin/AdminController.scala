package controllers.admin

import licensing.License
import play.api.i18n.MessagesApi
import play.api.mvc.Action

import scala.concurrent.Future

@javax.inject.Singleton
class AdminController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseAdminController {
  def index() = withAdminSession { (username, request) =>
    implicit val req = request
    Future.successful(Ok(views.html.admin.index(username)))
  }

  def enable(id: String) = Action.async { implicit request =>
    if (request.getQueryString("p").contains("np")) {
      Future.successful(Redirect(controllers.admin.routes.AdminController.index()).withSession("admin-role" -> id))
    } else {
      Future.successful(Redirect(controllers.routes.SiteController.index()).withNewSession)
    }
  }

  def sandbox() = Action.async { implicit request =>
    val license = License(name = "License Holder", email = "email@databaseflow.com")
    Future.successful(Ok(views.html.email.personalLicenseNotification(license)))
  }
}
