package controllers

import play.api.i18n.MessagesApi
import play.twirl.api.Html
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.audit.DownloadService

import scala.concurrent.Future

@javax.inject.Singleton
class DownloadController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  private[this] val baseUrl = s"https://s3.amazonaws.com/databaseflow/1.0.0/"

  def index() = act("download-index") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.downloads(isAdmin)))
  }

  def download(filename: String) = act(s"download-$filename") { implicit request =>
    val (isOk, platform) = filename match {
      case "DatabaseFlow.dmg" => true -> "macos"
      case "DatabaseFlow.jar" => true -> "jar"
      case "DatabaseFlow.pkg" => true -> "appstore"
      case "DatabaseFlow.zip" => true -> "windows"
      case "databaseflow.docker.gz" => true -> "docker"
      case "databaseflow.server.zip" => true -> "universal"
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
