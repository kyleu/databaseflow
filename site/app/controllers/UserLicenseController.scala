package controllers

import java.util.UUID

import licensing.{License, LicenseEdition, LicenseGenerator}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

object UserLicenseController {
  case class LicenseForm(name: String, email: String, edition: LicenseEdition)

  val licenseForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> email,
      "edition" -> nonEmptyText.transform(
        (s) => LicenseEdition.withName(s),
        (x: LicenseEdition) => x.toString
      )
    )(LicenseForm.apply)(LicenseForm.unapply)
  )
}

@javax.inject.Singleton
class UserLicenseController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def requestForm() = Action.async { implicit request =>
    Future.successful(Ok(views.html.purchase.licenseForm(UserLicenseController.licenseForm)))
  }

  def licenseRequest() = Action.async { implicit request =>
    UserLicenseController.licenseForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.purchase.licenseForm(formWithErrors)))
      },
      f => {
        val license = License(name = f.name, email = f.email, edition = f.edition)
        LicenseGenerator.saveLicense(license)
        Future.successful(Redirect(controllers.routes.UserLicenseController.licenseView(license.id)))
      }
    )
  }

  def licenseView(id: UUID) = Action.async { implicit request =>
    val license = LicenseGenerator.loadLicense(id)
    val content = LicenseGenerator.getContent(id)
    Future.successful(Ok(views.html.purchase.licenseView(license, new String(content))))
  }

  def licenseDownload(id: UUID) = Action.async { implicit request =>
    val content = LicenseGenerator.getContent(id)
    Future.successful(Ok(content).as("application/octet-stream").withHeaders("Content-Disposition" -> "attachment; filename=databaseflow.license"))
  }
}
