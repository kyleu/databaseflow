package controllers

import play.api.i18n.MessagesApi
import play.twirl.api.Html
import services.audit.InstallService

import scala.concurrent.Future

object UpdateController {
  val version = 1
}

@javax.inject.Singleton
class UpdateController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def version() = act("version.request") { implicit request =>
    Future.successful(Ok(UpdateController.version.toString))
  }

  def install() = act("install") { implicit request =>
    InstallService.add(request.remoteAddress, "web")
    Future.successful(Ok(Html(s"Thanks for installing ${utils.Config.projectName}!")))
  }
}
