package controllers

import play.api.i18n.MessagesApi

import scala.concurrent.Future

@javax.inject.Singleton
class DownloadController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  private[this] val url = s"https://github.com/KyleU/databaseflow/releases"

  def index() = act("download-index") { implicit request =>
    Future.successful(Redirect(url))
  }

  def download(filename: String) = act(s"download-$filename") { implicit request =>
    Future.successful(Redirect(url))
  }
}
