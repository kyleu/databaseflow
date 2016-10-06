package controllers.admin

import controllers.BaseSiteController
import play.api.i18n.MessagesApi
import services.audit.{DownloadService, InstallService, RequestService, StartupService}

import scala.concurrent.Future

@javax.inject.Singleton
class AuditController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def listRequests() = withAdminSession("request-list") { (username, request) =>
    implicit val req = request
    val requests = RequestService.list()
    Future.successful(Ok(views.html.admin.audit.requests(requests)))
  }

  def listDownloads() = withAdminSession("download-list") { (username, request) =>
    implicit val req = request
    val downloads = DownloadService.list()
    Future.successful(Ok(views.html.admin.audit.downloads(downloads)))
  }

  def listInstalls() = withAdminSession("install-list") { (username, request) =>
    implicit val req = request
    val installs = InstallService.list()
    Future.successful(Ok(views.html.admin.audit.installs(installs)))
  }

  def listStartups() = withAdminSession("startup-list") { (username, request) =>
    implicit val req = request
    val startups = StartupService.list()
    Future.successful(Ok(views.html.admin.audit.startups(startups)))
  }
}
