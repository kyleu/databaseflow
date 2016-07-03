package controllers

import services.licensing.LicenseService
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class LicenseController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def form() = withSession("license-form") { implicit request =>
    val lic = LicenseService.getLicense
    val licContent = LicenseService.getLicenseContent
    Future.successful(Ok(views.html.license.form(request.identity, ctx.config.debug, lic, licContent)))
  }

  def save() = withSession("license-form") { implicit request =>
    Future.successful(Redirect(controllers.routes.HomeController.index()))
  }
}
