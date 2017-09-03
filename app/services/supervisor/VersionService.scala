package services.supervisor

import models.settings.SettingKey
import util.FutureUtils.defaultContext
import play.api.libs.ws.WSClient
import services.settings.SettingsService
import util.Logging

import scala.concurrent.Future

object VersionService extends Logging {
  //private[this] val domain = "databaseflow.com"
  private[this] val domain = "localhost:10000"
  private[this] def queryString = s"?id=${SettingsService(SettingKey.InstallId)}"
  private[this] def url = s"https://$domain/version" + queryString
  private[this] def insecureUrl = s"http://$domain/version" + queryString

  private[this] def loadServerVersion(ws: WSClient) = ws.url(url).get().map(_.body).recoverWith {
    case _ => ws.url(insecureUrl).get().map(_.body).recoverWith {
      case ex =>
        log.warn(s"Unable to retreive latest version from public server: [${ex.getMessage}].")
        Future.successful("?")
    }
  }

  def upgradeIfNeeded(ws: WSClient) = loadServerVersion(ws).map { v =>
    if (util.Config.projectVersion == v) {
      log.info(s"You are currently running the latest version of ${util.Config.projectName}.")
    } else {
      log.warn(s"A new version of ${util.Config.projectName} is available ($v). Head to [${util.Config.projectUrl}] to download the new version.")
    }
  }
}
