package controllers.admin

import java.util.UUID

import licensing.{License, LicenseEdition, LicenseGenerator}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.MessagesApi

import scala.concurrent.Future

object LicenseController {
  val licenseForm = Form(
    mapping(
      "id" -> uuid,
      "user" -> email,
      "edition" -> nonEmptyText.transform(
        (s) => LicenseEdition.Personal: LicenseEdition,
        (x: LicenseEdition) => x.toString
      ),
      "issued" -> longNumber,
      "version" -> ignored(1)
    )(License.apply)(License.unapply)
  )
}

@javax.inject.Singleton
class LicenseController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseAdminController {
  def list() = withAdminSession { (username, request) =>
    implicit val req = request
    val licenses = LicenseGenerator.listLicenses().map { x =>
      LicenseGenerator.loadLicense(x)
    }
    Future.successful(Ok(views.html.admin.licenses(licenses)))
  }

  def form() = withAdminSession { (username, request) =>
    implicit val req = request
    Future.successful(Ok(views.html.admin.licenseForm()))
  }

  def save() = withAdminSession { (username, request) =>
    implicit val req = request
    val action = LicenseController.licenseForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.admin.licenseForm()),
      license => {
        LicenseGenerator.saveLicense(license)
        Redirect(controllers.admin.routes.LicenseController.list())
      }
    )
    Future.successful(action)
  }

  def download(id: UUID) = withAdminSession { (username, request) =>
    implicit val req = request
    val content = LicenseGenerator.getContent(id)
    Future.successful(Ok(content).as("application/octet-stream").withHeaders("Content-Disposition" -> s"attachment; filename=$id.license"))
  }

  def remove(id: UUID) = withAdminSession { (username, request) =>
    implicit val req = request
    LicenseGenerator.removeLicense(id)
    Future.successful(Redirect(controllers.admin.routes.LicenseController.list()).flashing("success" -> "License removed."))
  }
}
