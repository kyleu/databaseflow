package utils

import java.util.TimeZone

import akka.actor.{ActorSystem, Props}
import com.codahale.metrics.SharedMetricRegistries
import com.mohiva.play.silhouette.api.Silhouette
import models.auth.AuthEnv
import models.settings.SettingKey
import models.ui.TopFrame
import org.joda.time.{DateTimeZone, LocalDateTime}
import play.api.Environment
import play.api.i18n.MessagesApi
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import services.config.ConfigFileService
import services.database.core.{MasterDatabase, ResultCacheDatabase}
import services.database.{DatabaseRegistry, MasterDdl}
import services.settings.SettingsService
import services.supervisor.{ActorSupervisor, VersionService}
import utils.metrics.Instrumented

import scala.concurrent.Future

object ApplicationContext {
  var initialized = false
  var maintenanceMode = false
}

@javax.inject.Singleton
class ApplicationContext @javax.inject.Inject() (
    val messagesApi: MessagesApi,
    val config: Configuration,
    val lifecycle: ApplicationLifecycle,
    val playEnv: Environment,
    val actorSystem: ActorSystem,
    val silhouette: Silhouette[AuthEnv],
    val ws: WSClient
) extends Logging {
  if (ApplicationContext.initialized) {
    log.info("Skipping initialization after failure.")
  } else {
    start()
  }

  val supervisor = actorSystem.actorOf(Props(classOf[ActorSupervisor], this), "supervisor")
  log.debug(s"Actor Supervisor [${supervisor.path}] started for [${utils.Config.projectId}].")

  private[this] def start() = {
    log.info(s"${Config.projectName} is starting.")
    ApplicationContext.initialized = true

    DateTimeZone.setDefault(DateTimeZone.UTC)
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    if ((!config.debug) && java.awt.Desktop.isDesktopSupported && (System.getProperty("show.gui", "false") == "true")) {
      TopFrame.open()
    }

    SharedMetricRegistries.remove("default")
    SharedMetricRegistries.add("default", Instrumented.metricRegistry)

    ConfigFileService.init()
    ResultCacheDatabase.open()

    lifecycle.addStopHook(() => Future.successful(stop()))

    MasterDatabase.open()
    MasterDdl.update(MasterDatabase.conn)
    SettingsService.load()
    SettingsService.getOrSet(SettingKey.InstallDate, {
      ws.url(utils.Config.projectUrl + "/install").get()
      new LocalDateTime().toString
    })

    VersionService.upgradeIfNeeded(ws)

    if ((!config.debug) && java.awt.Desktop.isDesktopSupported && (System.getProperty("show.gui", "false") != "true")) {
      Future {
        Thread.sleep(2000)
        java.awt.Desktop.getDesktop.browse(new java.net.URI("http://localhost:4260"))
      }
    }
  }

  private[this] def stop() = {
    DatabaseRegistry.close()
    SharedMetricRegistries.remove("default")
  }
}
