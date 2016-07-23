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
      // No op, request failed.
    } else if (localVersion == v) {
      log.info(s"You are currently running the latest version of Database Flow.")
    } else if (localVersion < v) {
      log.warn(s"A new version of Database Flow is available. Head to [https://databaseflow.com] to download the new version.")
    } else {
      log.warn(s"You're somehow running a newer version of Database Flow than the public version.")
    }
  }
}
