package controllers

import com.mohiva.play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}
import models.auth.AuthEnv
import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.licensing.LicenseService
import utils.metrics.Instrumented
import utils.{ApplicationContext, Logging}

import scala.concurrent.Future

abstract class BaseController() extends Controller with I18nSupport with Instrumented with Logging {
  def ctx: ApplicationContext

  override def messagesApi = ctx.messagesApi

  def withAdminSession(action: String)(block: (SecuredRequest[AuthEnv, AnyContent]) => Future[Result]) = {
    ctx.silhouette.SecuredAction.async { implicit request =>
      metrics.timer(action).timeFuture {
        val authorized = LicenseService.isPersonalEdition || request.identity.isAdmin
        if (authorized) {
          block(request)
        } else {
          Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("error" -> "You must have admin rights to access that page."))
        }
      }
    }
  }

  def withoutSession(action: String)(block: UserAwareRequest[AuthEnv, AnyContent] => Future[Result]) = {
    ctx.silhouette.UserAwareAction.async { implicit request =>
      metrics.timer(action).timeFuture {
        block(request)
      }
    }
  }

  def withSession(action: String)(block: (SecuredRequest[AuthEnv, AnyContent]) => Future[Result]) = {
    ctx.silhouette.UserAwareAction.async { implicit request =>
      if (!LicenseService.hasLicense) {
        Future.successful(Redirect(controllers.routes.LicenseController.form()).flashing(
          "success" -> s"Please configure your license for ${utils.Config.projectName}."
        ))
      } else {
        request.identity match {
          case Some(u) => metrics.timer(action).timeFuture {
            val auth = request.authenticator.getOrElse(throw new IllegalStateException("Somehow not logged in..."))
            block(SecuredRequest(u, auth, request))
          }
          case None => Future.successful(Redirect(controllers.auth.routes.AuthenticationController.signInForm()).flashing(
            "error" -> s"You must sign in or register before accessing ${utils.Config.projectName}."
          ))
        }
      }
    }
  }
}
