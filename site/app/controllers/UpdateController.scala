package controllers

import play.api.i18n.MessagesApi

import scala.concurrent.Future

object UpdateController {
  val majorVersion = 1
  val minorVersion = 0
}

@javax.inject.Singleton
class UpdateController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def version() = act("version.request") { implicit request =>
    Future.successful(Ok(s"${UpdateController.majorVersion}.${UpdateController.minorVersion}"))
  }
}
