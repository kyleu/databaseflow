package controllers

import play.api.i18n.MessagesApi
import play.twirl.api.Html

import scala.concurrent.Future

@javax.inject.Singleton
class DownloadController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  private[this] val downloadDir = {
    val server = new java.io.File("/home/ubuntu/deploy/databaseflow-downloads")
    if (server.exists && server.isDirectory) {
      server
    } else {
      val local = new java.io.File("./build")
      if (local.exists && local.isDirectory) {
        log.info("Using local directory for downloads.")
        local
      } else {
        throw new IllegalStateException("Cannot find download directory.")
      }
    }
  }

  def index() = act("download-index") { implicit request =>
    val isAdmin = isAdminUser(request).isDefined
    Future.successful(Ok(views.html.downloads(isAdmin)))
  }

  def download(os: String) = act(s"download-$os") { implicit request =>
    val filename = os match {
      case "databaseflow.dmg" => "Database Flow.dmg"
      case "databaseflow.exe" => "Database Flow.exe"
      case "databaseflow.tar" => "Database Flow.tar"
      case "databaseflow.docker.gz" => "databaseflow.docker.gz"
      case "databaseflow.zip" => "databaseflow.zip"
      case x => throw new IllegalStateException(s"Unknown os [$os].")
    }
    val file = new java.io.File(downloadDir, filename)
    if (file.exists) {
      Future.successful(Ok.sendFile(file))
    } else {
      Future.successful(NotFound(Html(s"<body>We're sorry, we couldn't find that download.<!-- ${file.getAbsolutePath} --></body>")))
    }
  }

  def update(path: String) = act(s"update") { implicit request =>
    val file = new java.io.File(downloadDir, "jwrapper/" + path)
    if (file.exists) {
      Future.successful(Ok.sendFile(file))
    } else {
      Future.successful(NotFound(Html(s"<body>We're sorry, we couldn't find that download.<!-- ${file.getAbsolutePath} --></body>")))
    }
  }
}
