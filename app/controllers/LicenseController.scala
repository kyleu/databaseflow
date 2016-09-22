package controllers

import models.settings.SettingKey
import services.licensing.LicenseService
import services.settings.SettingsService
import services.user.UserService
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
        val license = LicenseService.readLicense()
        val ret = if (UserService.instance.exists(_.userCount == 0)) {
          val msg = messagesApi("license.success") + " " + messagesApi("license.register", utils.Config.projectName)
          Redirect(controllers.auth.routes.RegistrationController.registrationForm(email = license.map(_.email))).flashing("success" -> msg)
        } else {
          Redirect(controllers.routes.HomeController.home()).flashing("success" -> messagesApi("license.success"))
        }
        Future.successful(ret)
      case Failure(x) =>
        val msg = x match {
          case _ if content.isEmpty => messagesApi("license.paste")
          case ex =>
            log.warn("Cannot parse license.", ex)
            messagesApi("license.error")
        }
        Future.successful(Redirect(controllers.routes.LicenseController.form()).flashing("error" -> msg))
    }
  }
}
