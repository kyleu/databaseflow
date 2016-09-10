package controllers

import models.settings.SettingKey
import services.licensing.LicenseService
import services.settings.SettingsService
import utils.ApplicationContext
import utils.web.FormUtils

import scala.concurrent.Future
import scala.util.{Failure, Success}

@javax.inject.Singleton
class LicenseController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def form() = withoutSession("license-form") { implicit request =>
    val lic = LicenseService.getLicense
    val licContent = LicenseService.getLicenseContent

    Future.successful(Ok(views.html.license.form(request.identity, lic, licContent)))
  }

  def save() = withoutSession("license-save") { implicit request =>
    val form = FormUtils.getForm(request)
    val content = form.getOrElse("content", throw new IllegalStateException(messagesApi("license.missing"))).trim

    LicenseService.parseLicense(content) match {
      case Success(l) =>
        SettingsService.set(SettingKey.LicenseContent, content)
        LicenseService.readLicense()
        Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("success" -> messagesApi("license.success")))
      case Failure(x) =>
        val msg = x match {
          case _ if content.isEmpty => messagesApi("license.paste")
          case ex => ex.getClass.getSimpleName + ": " + ex.getMessage
        }
        Future.successful(Redirect(controllers.routes.LicenseController.form()).flashing("error" -> msg))
    }
  }
}
