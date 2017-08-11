package controllers

import play.api.i18n.MessagesApi
import play.twirl.api.Html
import util.FutureUtils.defaultContext
import services.audit.DownloadService

import scala.concurrent.Future

object DownloadController {
  val version = "1.0.0"
}

@javax.inject.Singleton
class DownloadController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  private[this] val baseUrl = s"https://github.com/KyleU/databaseflow/releases/download/v${DownloadController.version}/"

  def index() = act("download-index") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.downloads(isAdmin)))
  }

  def download(filename: String) = act(s"download-$filename") { implicit request =>
    val (isOk, platform) = filename match {
      case "databaseflow.dmg" => true -> "macos"
      case "databaseflow.jar" => true -> "jar"
      case "databaseflow.pkg" => true -> "appstore"
      case "databaseflow.windows.zip" => true -> "windows"
      case "databaseflow.docker.gz" => true -> "docker"
      case "databaseflow.universal.zip" => true -> "universal"
      case x => false -> s"unknown:$x"
    }
    Future(DownloadService.add(request.remoteAddress, platform))
    if (isOk) {
      Future.successful(Redirect(baseUrl + filename))
    } else {
      Future.successful(NotFound(Html(s"<body>We're sorry, we couldn't find that download.<!-- $filename --></body>")))
    }
  }
}
