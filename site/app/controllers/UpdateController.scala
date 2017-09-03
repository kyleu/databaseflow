package controllers

import java.util.UUID

import play.api.i18n.MessagesApi
import play.twirl.api.Html
import services.audit.{InstallService, StartupService}

import scala.concurrent.Future
import scala.util.control.NonFatal

object UpdateController {
  val version = "1.0.0"
}

@javax.inject.Singleton
class UpdateController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  private[this] val invalid = UUID.fromString("00000000-0000-0000-0000-000000000000")

  def version(inst: Option[String]) = act("version.request") { implicit request =>
    val installId = inst.flatMap { x =>
      try { Some(UUID.fromString(x)) } catch { case NonFatal(_) => None }
    }.getOrElse(invalid)
    StartupService.add(installId, request.remoteAddress)
    Future.successful(Ok(UpdateController.version))
  }

  def install() = act("install") { implicit request =>
    InstallService.add(request.remoteAddress, "web")
    Future.successful(Ok(Html(s"Thanks for installing ${util.Config.projectName}!")))
  }
}
