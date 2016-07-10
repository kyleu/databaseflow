package controllers.admin

import java.util.UUID

import controllers.BaseSiteController
import licensing.{License, LicenseEdition, LicenseGenerator}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.MessagesApi
import services.notification.NotificationService
import utils.web.PlayFormUtils

import scala.concurrent.Future

object LicenseController {
  val licenseForm = Form(
    mapping(
      "id" -> uuid,
      "name" -> nonEmptyText,
      "email" -> email,
      "edition" -> nonEmptyText.transform(
        (s) => LicenseEdition.withName(s),
        (x: LicenseEdition) => x.toString
      ),
      "issued" -> longNumber,
      "version" -> ignored(1)
    )(License.apply)(License.unapply)
  )
}

@javax.inject.Singleton
class LicenseController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi, notification: NotificationService) extends BaseSiteController {
  def list() = withAdminSession("admin.license.list") { (username, request) =>
    implicit val req = request
    val licenses = LicenseGenerator.listLicenses().map(LicenseGenerator.loadLicense)
    Future.successful(Ok(views.html.admin.licenses(licenses)))
  }

  def form() = withAdminSession("admin.license.form") { (username, request) =>
    implicit val req = request
    Future.successful(Ok(views.html.admin.licenseForm()))
  }

  def save() = withAdminSession("admin.license.save") { (username, request) =>
    implicit val req = request
    val action = LicenseController.licenseForm.bindFromRequest.fold(
      formWithErrors => {
        val msg = PlayFormUtils.errorsToString(formWithErrors.errors)
        Redirect(controllers.admin.routes.LicenseController.form()).flashing("error" -> msg)
      },
      license => {
        LicenseGenerator.saveLicense(license)
        Redirect(controllers.admin.routes.LicenseController.list())
      }
    )
    Future.successful(action)
  }

  def download(id: UUID) = withAdminSession("admin.license.download") { (username, request) =>
    implicit val req = request
    val content = LicenseGenerator.getContent(id)
    Future.successful(Ok(content).as("application/octet-stream").withHeaders("Content-Disposition" -> s"attachment; filename=$id.license"))
  }

  def email(licenseId: UUID) = withAdminSession("admin.license.email") { (username, request) =>
    val license = LicenseGenerator.loadLicense(licenseId)
    val licenseContent = new String(LicenseGenerator.getContent(licenseId))
    notification.onLicenseCreate(license.id, license.name, license.email, license.edition.title, license.issued, license.version, licenseContent)
    Future.successful(Ok("Ok!"))
  }

  def remove(id: UUID) = withAdminSession("admin.license.remove") { (username, request) =>
    implicit val req = request
    LicenseGenerator.removeLicense(id)
    Future.successful(Redirect(controllers.admin.routes.LicenseController.list()).flashing("success" -> "License removed."))
  }
}
