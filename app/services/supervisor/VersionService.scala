package services.supervisor

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import utils.Logging

import scala.concurrent.Future

object VersionService extends Logging {
  val localVersion = 1
  private[this] val url = "https://databaseflow.com/version"
  private[this] val insecureUrl = "http://databaseflow.com/version"

  def loadServerVersion(ws: WSClient) = ws.url(url).get().map(_.body.toInt).recoverWith {
    case x => ws.url(insecureUrl).get().map(_.body.toInt).recoverWith {
      case ex =>
        log.info(s"Unable to retreive latest version from public server: [${ex.getMessage}].")
        Future.successful(-1)
    }
  }

  def upgradeIfNeeded(ws: WSClient) = loadServerVersion(ws).map { v =>
    if (v == -1) {
      log.info(s"We can't tell if you're running the latest version of ${utils.Config.projectName}.")
    } else if (localVersion == v) {
      log.info(s"You are currently running the latest version of ${utils.Config.projectName}.")
    } else if (localVersion < v) {
      log.warn(s"A new version of ${utils.Config.projectName} is available. Head to [${utils.Config.projectUrl}] to download the new version.")
    } else {
      log.warn(s"You're somehow running a newer version of ${utils.Config.projectName} than the public version.")
    }
  }
}
