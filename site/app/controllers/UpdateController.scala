package controllers

import play.api.i18n.MessagesApi

import scala.concurrent.Future

object UpdateController {
  val version = 1
}

@javax.inject.Singleton
class UpdateController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def version() = act("version.request") { implicit request =>
    Future.successful(Ok(UpdateController.version.toString))
  }
}
