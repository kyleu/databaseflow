package controllers.admin

import licensing.{ License, LicenseGenerator }
import play.api.i18n.MessagesApi
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.Future

object LicenseController {
  val licenseForm = Form(
    mapping(
      "email" -> email
    )(License.apply)(License.unapply)
  )
}

@javax.inject.Singleton
class LicenseController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseAdminController {
  def list() = withSession("license-list") { (username, request) =>
    implicit val req = request
    val licenses = LicenseGenerator.listLicenses()
    Future.successful(Ok(views.html.admin.licenses(licenses)))
  }

  def form() = withSession("license-form") { (username, request) =>
    implicit val req = request
    Future.successful(Ok(views.html.admin.licenseForm()))
  }

  def save() = withSession("license-save") { (username, request) =>
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

  def remove(id: String) = withSession("license-remove") { (username, request) =>
    implicit val req = request
    LicenseGenerator.removeLicense(id)
    Future.successful(Redirect(controllers.admin.routes.LicenseController.list()))
  }
}
