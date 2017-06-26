package controllers

import com.mohiva.play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}
import models.auth.AuthEnv
import play.api.i18n.I18nSupport
import utils.FutureUtils.defaultContext
import play.api.mvc._
import services.user.UserService
import utils.metrics.Instrumented
import utils.{ApplicationContext, Logging}

import scala.concurrent.Future

abstract class BaseController() extends InjectedController with I18nSupport with Instrumented with Logging {
  def ctx: ApplicationContext

  override implicit val messagesApi = ctx.messagesApi

  def withAdminSession(action: String)(block: (SecuredRequest[AuthEnv, AnyContent]) => Future[Result]) = {
    ctx.silhouette.SecuredAction.async(parse.anyContent) { implicit request =>
      metrics.timer(action).timeFuture {
        if (request.identity.isAdmin) {
          block(request)
        } else {
          Future.successful(Redirect(controllers.routes.HomeController.home()).flashing("error" -> messagesApi("error.admin.required")(request.lang)))
        }
      }
    }
  }

  def withoutSession(action: String)(block: UserAwareRequest[AuthEnv, AnyContent] => Future[Result]) = {
    ctx.silhouette.UserAwareAction.async(parse.anyContent) { implicit request =>
      metrics.timer(action).timeFuture {
        checkMaintenanceMode(action)(block(request))
      }
    }
  }

  def withSession(action: String)(block: (SecuredRequest[AuthEnv, AnyContent]) => Future[Result]) = {
    ctx.silhouette.UserAwareAction.async(parse.anyContent) { implicit request =>
      metrics.timer(action).timeFuture {
        request.identity match {
          case Some(u) =>
            val auth = request.authenticator.getOrElse(throw new IllegalStateException(messagesApi("error.not.logged.in")(request.lang)))
            checkMaintenanceMode(action)(block(SecuredRequest(u, auth, request)))
          case None =>
            val result = UserService.instance.map(_.userCount) match {
              case Some(x) if x == 0 => Redirect(controllers.auth.routes.RegistrationController.registrationForm())
              case _ => Redirect(controllers.auth.routes.AuthenticationController.signInForm())
            }
            Future.successful(result.flashing(
              "error" -> messagesApi("error.must.sign.in", utils.Config.projectName)(request.lang)
            )).map(r => if (!request.uri.contains("signin")) {
              r.withSession(r.session + ("returnUrl" -> request.uri))
            } else {
              log.info(s"Skipping returnUrl for external url [${request.uri}].")
              r
            })
        }
      }
    }
  }

  private[this] def checkMaintenanceMode(action: String)(f: => Future[Result])(implicit request: Request[AnyContent]) = {
    if (ApplicationContext.maintenanceMode) {
      Future.successful(Ok(views.html.maintenance()))
    } else {
      metrics.timer(action).timeFuture(f)
    }
  }
}
