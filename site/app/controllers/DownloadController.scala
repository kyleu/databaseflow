package controllers

import play.api.i18n.MessagesApi
import play.twirl.api.Html

import scala.concurrent.Future

@javax.inject.Singleton
class DownloadController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  private[this] val baseUrl = s"https://s3.amazonaws.com/databaseflow/1.0.0/"

  def index() = act("download-index") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.downloads(isAdmin)))
  }

  def download(filename: String) = act(s"download-$filename") { implicit request =>
    val isOk = filename match {
      case "DatabaseFlow.dmg" => true
      case "DatabaseFlow.jar" => true
      case "DatabaseFlow.pkg" => true
      case "DatabaseFlow.zip" => true
      case "databaseflow.docker.gz" => true
      case "databaseflow.server.zip" => true
      case _ => false
    }
    if (isOk) {
      Future.successful(Redirect(baseUrl + filename))
    } else {
      Future.successful(NotFound(Html(s"<body>We're sorry, we couldn't find that download.<!-- $filename --></body>")))
    }
  }
}
