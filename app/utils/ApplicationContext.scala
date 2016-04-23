package utils

import java.util.TimeZone

import akka.actor.{ ActorRef, ActorSystem, Props }
import com.codahale.metrics.SharedMetricRegistries
import com.mohiva.play.silhouette.api.Silhouette
import org.joda.time.DateTimeZone
import play.api.Environment
import play.api.http.HttpRequestHandler
import play.api.i18n.MessagesApi
import play.api.inject.ApplicationLifecycle
import play.api.mvc.{ Action, RequestHeader, Results }
import play.api.routing.Router
import models.auth.AuthEnv
import services.database.MasterDatabase
import services.notification.NotificationService
import services.settings.SettingsService
import services.supervisor.ActorSupervisor
import utils.metrics.Instrumented

import scala.concurrent.Future

object ApplicationContext {
  var initialized = false

  class SimpleHttpRequestHandler @javax.inject.Inject() (router: Router) extends HttpRequestHandler {
    def handlerForRequest(request: RequestHeader) = {
      router.routes.lift(request) match {
        case Some(handler) => (request, handler)
        case None => (request, Action(Results.NotFound))
      }
    }
  }
}

@javax.inject.Singleton
class ApplicationContext @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val config: Config,
    val settings: SettingsService,
    val lifecycle: ApplicationLifecycle,
    val playEnv: Environment,
    val notificationService: NotificationService,
    val actorSystem: ActorSystem,
    val silhouette: Silhouette[AuthEnv]
) extends Logging {
  if (ApplicationContext.initialized) {
    log.info(s"Skipping initialization after failure.")
  } else {
    log.info(s"${Config.projectName} is starting.")
    ApplicationContext.initialized = true

    DateTimeZone.setDefault(DateTimeZone.UTC)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    SharedMetricRegistries.remove("default")
    SharedMetricRegistries.add("default", Instrumented.metricRegistry)

    MasterDatabase.open()
    settings.load()

    lifecycle.addStopHook(() => Future.successful(stop()))
  }

  val supervisor = actorSystem.actorOf(Props(classOf[ActorSupervisor], this), "supervisor")
  log.info(s"Actor Supervisor [${supervisor.path}] started for [${utils.Config.projectId}].")

  private[this] def stop() = {
    MasterDatabase.close()
    SharedMetricRegistries.remove("default")
  }
}
