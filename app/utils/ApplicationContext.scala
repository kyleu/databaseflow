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
import services.history.HistoryDatabase
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

  HistoryDatabase.open()

  lifecycle.addStopHook(() => Future.successful(stop()))

  private[this] def stop() = {
    HistoryDatabase.close()
    SharedMetricRegistries.remove("default")
  }
}
