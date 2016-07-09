package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import utils.Logging

import scala.concurrent.Future

@javax.inject.Singleton
class DownloadController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends Controller with I18nSupport with Logging {
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

  def download(os: String, variant: String) = Action.async { implicit request =>
    val filename = os match {
      case "osx" => variant match {
        case "service" => "osx/databaseflow.zip"
        case "gui" => "osx/databaseflow.dmg"
        case x => throw new IllegalStateException(s"Unknown variant [$variant].")
      }
      case "linux" => variant match {
        case "service" => "linux/databaseflow.tgz"
        case "deb" => "linux/databaseflow_1.0.0_all.deb"
        case x => throw new IllegalStateException(s"Unknown variant [$variant].")
      }
      case "windows" => variant match {
        case "service" => "databaseflow.zip"
        case "gui" => "databaseflow.msi"
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
