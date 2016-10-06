package services.supervisor

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import services.licensing.LicenseService
import utils.Logging

import scala.concurrent.Future

object VersionService extends Logging {
  val localVersion = "1.0.0"
  private[this] val url = "https://databaseflow.com/version"
  private[this] val insecureUrl = "http://databaseflow.com/version"
  private[this] def withLicense(url: String) = LicenseService.getLicense.map(url + "?license=" + _.id).getOrElse(url)

  def loadServerVersion(ws: WSClient) = ws.url(withLicense(url)).get().map(_.body).recoverWith {
    case x => ws.url(withLicense(insecureUrl)).get().map(_.body).recoverWith {
      case ex =>
        log.info(s"Unable to retreive latest version from public server: [${ex.getMessage}].")
        Future.successful("?")
    }
  }

  def upgradeIfNeeded(ws: WSClient) = loadServerVersion(ws).map { v =>
    if (localVersion == v) {
      log.info(s"You are currently running the latest version of ${utils.Config.projectName}.")
    } else {
      log.warn(s"A new version of ${utils.Config.projectName} is available ($v). Head to [${utils.Config.projectUrl}] to download the new version.")
    }
  }
}
