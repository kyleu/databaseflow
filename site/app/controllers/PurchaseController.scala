package controllers

import licensing.LicenseEdition
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller, Request}

import scala.concurrent.Future

object PurchaseController {
  case class LicenseForm(user: String, edition: LicenseEdition)

  val licenseForm = Form(
    mapping(
      "user" -> email,
      "edition" -> nonEmptyText.transform(
        (s) => LicenseEdition.withName(s),
        (x: LicenseEdition) => x.toString
      )
    )(LicenseForm.apply)(LicenseForm.unapply)
  )

  def getForm(request: Request[AnyContent]) = request.body.asFormUrlEncoded match {
    case Some(f) => f.mapValues(x => x.headOption.getOrElse(throw new IllegalStateException("Empty form element.")))
    case None => throw new IllegalStateException("Missing form post.")
  }
}

@javax.inject.Singleton
class PurchaseController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def pricing() = Action.async { implicit request =>
    Future.successful(Ok("OK"))
  }

  def purchaseTeamEdition() = Action.async { implicit request =>
    Future.successful(Ok("OK"))
  }

  def requestForm() = Action.async { implicit request =>
    Future.successful(Ok("OK"))
  }

  def licenseRequest() = Action.async { implicit request =>
    val form = PurchaseController.getForm(request)
    Future.successful(Ok("OK"))
  }
}
