package controllers.admin

import controllers.BaseSiteController
import play.api.i18n.MessagesApi
import services.logging.LogService

import scala.concurrent.Future

@javax.inject.Singleton
class LogController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def list() = withAdminSession("admin.log.list") { (username, request) =>
    implicit val req = request
    val files = LogService.listFiles()
    Future.successful(Ok(views.html.admin.log.logList(files)))
  }

  def view(name: String) = withAdminSession("admin.log.view") { (username, request) =>
    implicit val req = request
    val logs = LogService.getLogs(name)
    Future.successful(Ok(views.html.admin.log.logView(name, logs)))
  }
}
