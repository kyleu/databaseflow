package utils

import java.util.TimeZone

import akka.actor.{ActorSystem, Props}
import com.codahale.metrics.SharedMetricRegistries
import com.mohiva.play.silhouette.api.Silhouette
import models.auth.AuthEnv
import org.joda.time.DateTimeZone
import play.api.Environment
import play.api.i18n.MessagesApi
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import services.config.ConfigFileService
import services.database.DatabaseRegistry
import services.database.core.ResultCacheDatabase
import services.supervisor.ActorSupervisor
import utils.metrics.Instrumented

import scala.concurrent.Future

object ApplicationContext {
  var initialized = false
}

@javax.inject.Singleton
class ApplicationContext @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val config: Config,
    val lifecycle: ApplicationLifecycle,
    val playEnv: Environment,
    val actorSystem: ActorSystem,
    val silhouette: Silhouette[AuthEnv],
    val ws: WSClient
) extends Logging {
  if (ApplicationContext.initialized) {
    log.info("Skipping initialization after failure.")
  } else {
    log.info(s"${Config.projectName} is starting.")
    ApplicationContext.initialized = true

    DateTimeZone.setDefault(DateTimeZone.UTC)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    SharedMetricRegistries.remove("default")
    SharedMetricRegistries.add("default", Instrumented.metricRegistry)

    ConfigFileService.init()
    ResultCacheDatabase.open()

    lifecycle.addStopHook(() => Future.successful(stop()))

    ActorSupervisor.startIfNeeded(ws)
  }

  val supervisor = actorSystem.actorOf(Props(classOf[ActorSupervisor], this), "supervisor")
  log.debug(s"Actor Supervisor [${supervisor.path}] started for [${utils.Config.projectId}].")

  private[this] def stop() = {
    DatabaseRegistry.close()
    SharedMetricRegistries.remove("default")
  }
}
