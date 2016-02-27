package utils

import java.util.TimeZone

import com.codahale.metrics.SharedMetricRegistries
import org.joda.time.DateTimeZone
import play.api.Mode
import play.api.http.HttpRequestHandler
import play.api.i18n.MessagesApi
import play.api.inject.ApplicationLifecycle
import play.api.mvc.{Action, RequestHeader, Results}
import play.api.routing.Router
import services.notification.NotificationService
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
    val notificationService: NotificationService
) extends Logging {
  log.info(s"${Config.projectName} is starting.")

  DateTimeZone.setDefault(DateTimeZone.UTC)
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  SharedMetricRegistries.remove("default")
  SharedMetricRegistries.add("default", Instrumented.metricRegistry)

  lifecycle.addStopHook(() => Future.successful(stop()))

  private[this] def stop() = {
    SharedMetricRegistries.remove("default")
  }

  private[this] def scheduleTask() = {
    import play.api.Play.{current => app}

    if (app.mode == Mode.Dev) {
      log.info("Dev mode, so not starting scheduled task.")
    } else {
      log.info("Scheduling task to run every minute, after five minutes.")
      //val task = app.injector.instanceOf[ScheduledTask]
      //val system = app.injector.instanceOf[ActorSystem]
      //system.scheduler.schedule(5.minutes, 1.minute, task)
    }
  }

  val supervisor = {
    //val instanceRef = Akka.system.actorOf(Props(classOf[ActorSupervisor], this), "supervisor")
    //log.info(s"Actor Supervisor [${instanceRef.path}] started for [${utils.Config.projectId}].")
    //instanceRef
  }
}
