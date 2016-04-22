package utils

import java.util.TimeZone

import akka.actor.{ ActorSystem, Props }
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
    val lifecycle: ApplicationLifecycle,
    val playEnv: Environment,
    val notificationService: NotificationService,
    val actorSystem: ActorSystem,
    val silhouette: Silhouette[AuthEnv]
) extends Logging {
  log.info(s"${Config.projectName} is starting.")

  DateTimeZone.setDefault(DateTimeZone.UTC)
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  SharedMetricRegistries.remove("default")
  SharedMetricRegistries.add("default", Instrumented.metricRegistry)

  MasterDatabase.open()

  lifecycle.addStopHook(() => Future.successful(stop()))

  lazy val supervisor = {
    val instanceRef = actorSystem.actorOf(Props(classOf[ActorSupervisor], this), "supervisor")
    log.info(s"Actor Supervisor [${instanceRef.path}] started for [${utils.Config.projectId}].")
    instanceRef
  }

  supervisor.toString

  private[this] def stop() = {
    MasterDatabase.close()
    SharedMetricRegistries.remove("default")
  }
}
