package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.user.{ Role, User, UserPreferences }
import nl.grons.metrics.scala.FutureMetrics
import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{ AnyContent, Result }
import utils.metrics.Instrumented
import utils.{ ApplicationContext, DateUtils, Logging }

import scala.concurrent.Future

trait DefaultEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}

abstract class BaseController() extends I18nSupport with Instrumented with FutureMetrics with Logging {
  def ctx: ApplicationContext

  override def messagesApi = ctx.messagesApi

  def withAdminSession(action: String)(block: (SecuredRequest[DefaultEnv, AnyContent]) => Future[Result]) = {
    ctx.silhouette.SecuredAction.async { implicit request =>
      timing(action) {
        val startTime = System.nanoTime
        if (request.identity.roles.contains(Role.Admin)) {
          block(request)
        } else {
          Future.successful(NotFound("404 Not Found"))
        }
      }
    }
  }

  def withSession(action: String)(block: (SecuredRequest[DefaultEnv, AnyContent]) => Future[Result]) = {
    ctx.silhouette.UserAwareAction.async { implicit request =>
      timing(action) {
        val response = request.identity match {
          case Some(user) =>
            val secured = SecuredRequest(user, request.authenticator.getOrElse(throw new IllegalStateException()), request)
            block(secured)
          case None =>
            val user = User(
              id = UUID.randomUUID(),
              username = None,
              preferences = UserPreferences.empty,
              profiles = Nil,
              created = DateUtils.now
            )

            val u = ctx.authEnv.userService.save(user)
            for {
              authenticator <- ctx.silhouette.env.authenticatorService.create(LoginInfo("anonymous", u.id.toString))
              value <- ctx.silhouette.env.authenticatorService.init(authenticator)
              result <- block(SecuredRequest(u, authenticator, request))
              authedResponse <- ctx.silhouette.env.authenticatorService.embed(value, result)
            } yield {
              ctx.silhouette.env.eventBus.publish(SignUpEvent(u, request))
              ctx.silhouette.env.eventBus.publish(LoginEvent(u, request))
              authedResponse
            }
        }
        response
      }
    }
  }
}
