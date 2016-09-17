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

  def download(os: String, variant: String) = act(s"download-$os-$variant") { implicit request =>
    val filename = os match {
      case "osx" => variant match {
        case "zip" => "osx/databaseflow.zip"
        case "dmg" => "osx/databaseflow.dmg"
        case x => throw new IllegalStateException(s"Unknown variant [$variant].")
      }
      case "linux" => variant match {
        case "tgz" => "linux/databaseflow.tgz"
        case "deb" => "linux/databaseflow_1.0.0_all.deb"
        case x => throw new IllegalStateException(s"Unknown variant [$variant].")
      }
      case "windows" => variant match {
        case "exe" => "windows/Database Flow.exe"
        case "zip2" => "windows/Database Flow.zip"
        case "zip" => "windows/databaseflow.zip"
        case "msi" => "windows/databaseflow.msi"
        case x => throw new IllegalStateException(s"Unknown variant [$variant].")
      }
      case "docker" => variant match {
        case "zip" => "databaseflow.docker.gz"
        case x => throw new IllegalStateException(s"Unknown variant [$variant].")
      }
      case x => throw new IllegalStateException(s"Unknown os [$os].")
    }
    val file = new java.io.File(downloadDir, filename)
    if (file.exists) {
      Future.successful(Ok.sendFile(file))
    } else {
      Future.successful(NotFound(Html(s"<body>We're sorry, we couldn't find that download.<!-- ${file.getAbsolutePath} --></body>")))
    }
  }
}
