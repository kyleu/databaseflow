package controllers

import java.util.UUID

import licensing.{License, LicenseEdition, LicenseGenerator}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.notification.NotificationService

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
class UserLicenseController @javax.inject.Inject() (implicit val messagesApi: MessagesApi, notificationService: NotificationService) extends BaseSiteController {

  def requestForm() = act("user-license-form") { implicit request =>
    Future.successful(Ok(views.html.purchase.licenseForm(UserLicenseController.licenseForm)))
  }

  def licenseRequest() = act("user-license-request") { implicit request =>
    UserLicenseController.licenseForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.purchase.licenseForm(formWithErrors)))
      },
      f => {
        val license = License(name = f.name, email = f.email, edition = f.edition)
        val licenseContent = LicenseGenerator.saveLicense(license)
        notificationService.onLicenseCreate(license.id, license.name, license.email, license.edition.title, license.issued, license.version, licenseContent)

        Future.successful(Redirect(controllers.routes.UserLicenseController.licenseView(license.id)))
      }
    )
  }

  def licenseView(id: UUID) = act("user-license-view") { implicit request =>
    val license = LicenseGenerator.loadLicense(id)
    val content = LicenseGenerator.getContent(id)
    Future.successful(Ok(views.html.purchase.licenseView(license, new String(content))))
  }

  def licenseDownload(id: UUID) = act("user-license-download") { implicit request =>
    val content = LicenseGenerator.getContent(id)
    Future.successful(Ok(content).as("application/octet-stream").withHeaders("Content-Disposition" -> "attachment; filename=databaseflow.license"))
  }
}
