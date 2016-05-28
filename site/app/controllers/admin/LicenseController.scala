package controllers.admin

import licensing.LicenseGenerator
import play.api.i18n.MessagesApi

import scala.concurrent.Future

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
    Future.successful(Ok(views.html.admin.licenseForm()))
  }
}
