package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

@javax.inject.Singleton
class PurchaseController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def pricing() = Action.async { implicit request =>
    Future.successful(Ok(views.html.purchase.purchase()))
  }

  def purchaseTeamEdition() = Action.async { implicit request =>
    Future.successful(Ok("OK"))
  }
}
