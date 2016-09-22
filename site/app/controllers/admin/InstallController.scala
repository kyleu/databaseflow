package controllers.admin

import controllers.BaseSiteController
import play.api.i18n.MessagesApi
import services.audit.InstallService

import scala.concurrent.Future

@javax.inject.Singleton
class InstallController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def list() = withAdminSession("install-list") { (username, request) =>
    implicit val req = request
    val installs = InstallService.list()
    Future.successful(Ok(views.html.admin.installs(installs)))
  }
}
