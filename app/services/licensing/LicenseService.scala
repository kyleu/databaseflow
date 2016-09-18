package services.licensing

import java.util.Base64

import licensing.{DecryptUtils, License, LicenseEdition}
import models.settings.SettingKey
import org.joda.time.{Days, LocalDateTime}
import services.config.ConfigFileService
import services.settings.SettingsService
import utils.Logging

import scala.util.{Failure, Success, Try}

object LicenseService extends Logging {
  private[this] var licenseContent: Option[String] = None
  private[this] var license: Option[License] = None
  private[this] var startDate: Option[(LocalDateTime, Boolean)] = None

  def readLicense() = {
    val content = SettingsService(SettingKey.LicenseContent)
    if (content.isEmpty) {
      log.warn(s"${utils.Config.projectName} Trial Edition started.")
      license = None
      licenseContent = None
    } else {
      license = parseLicense(content) match {
        case Success(lic) =>
          log.warn(s"${utils.Config.projectName} ${lic.edition.title}, registered to [${lic.name}] (${lic.email}).")
          licenseContent = Some(content)
          Some(lic)
        case Failure(x) =>
          log.warn(s"Unable to parse license [$content].", x)
          None
      }
      startDate = license.map { l =>
        val installDate = SettingsService(SettingKey.InstallDate) match {
          case "" =>
            val d = new LocalDateTime()
            SettingsService.set(SettingKey.InstallDate, d.toString)
            d
          case x => LocalDateTime.parse(x)
        }
        val issueDate = new LocalDateTime(l.issued)
        val d = if (installDate.isBefore(issueDate)) { installDate } else { issueDate }
        d -> (Days.daysBetween(new LocalDateTime(), d).getDays > 30)
      }
    }
    if (ConfigFileService.isDocker) {
      log.warn(" - Head to http://[docker address]:4260 to get started!")
      log.warn(" - Since this is a docker container, you'll need to expose port 4260, by using the command flag [-p 4260:4260].")
    } else {
      log.warn(" - Head to http://localhost:4260 to get started!")
    }
    license
  }

  def parseLicense(content: String) = Try {
    val decoded = Base64.getDecoder.decode(content)
    val decrypted = DecryptUtils.decrypt(decoded)
    License.fromString(decrypted) match {
      case Success(l) => l
      case Failure(x) => throw x
    }
  }

  def getLicense = license
  def getLicenseContent = licenseContent
  def hasLicense = license.isDefined
  def isTrial = license.exists(_.edition == LicenseEdition.Trial)
  def isPersonalEdition = license.exists(_.edition == LicenseEdition.Personal)
  def isTeamEdition = license.exists(_.edition == LicenseEdition.Team)
  def expired = startDate.exists(_._2)
}
