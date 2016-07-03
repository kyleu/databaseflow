package controllers

import models.settings.SettingKey
import services.licensing.LicenseService
import services.settings.SettingsService
import utils.ApplicationContext

import scala.concurrent.Future
import scala.util.{Failure, Success}

@javax.inject.Singleton
class LicenseController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def form() = withoutSession("license-form") { implicit request =>
    val lic = LicenseService.getLicense
    val licContent = LicenseService.getLicenseContent
    Future.successful(Ok(views.html.license.form(ctx.config.debug, lic, licContent)))
  }

  def save() = withoutSession("license-form") { implicit request =>
    val form = request.body.asFormUrlEncoded match {
      case Some(f) => f
      case None => throw new IllegalStateException("Missing license form.")
    }
    val content = form.get("content").flatMap(_.headOption).getOrElse(throw new IllegalStateException("Missing license content.")).trim

    LicenseService.parseLicense(content) match {
      case Success(l) =>
        SettingsService.set(SettingKey.LicenseContent, content)
        LicenseService.readLicense()
        Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("success" -> "License updated successfully."))
      case Failure(x) =>
        val msg = x match {
          case _ if content.isEmpty => "Please paste your license in the form below."
          case ex => ex.getClass.getSimpleName + ": " + ex.getMessage
        }
        Future.successful(Redirect(controllers.routes.LicenseController.form()).flashing("error" -> msg))
    }
  }
}
