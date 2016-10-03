package controllers

import java.util.UUID

import licensing.LicenseGenerator
import play.api.i18n.MessagesApi

import scala.concurrent.Future

@javax.inject.Singleton
class UserLicenseController @javax.inject.Inject() (implicit val messagesApi: MessagesApi) extends BaseSiteController {
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
