package controllers

import java.util.UUID

import play.api.i18n.MessagesApi
import play.twirl.api.Html
import services.audit.{InstallService, StartupService}

import scala.concurrent.Future

object UpdateController {
  val version = "1.0.0"
}

@javax.inject.Singleton
class UpdateController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def version(license: Option[UUID]) = act("version.request") { implicit request =>
    StartupService.add(request.remoteAddress, license)
    Future.successful(Ok(UpdateController.version))
  }

  def install() = act("install") { implicit request =>
    InstallService.add(request.remoteAddress, "web")
    Future.successful(Ok(Html(s"Thanks for installing ${utils.Config.projectName}!")))
  }
}
