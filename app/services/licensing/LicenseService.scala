package services.licensing

import java.util.Base64

import licensing.{DecryptUtils, License, LicenseEdition}
import models.settings.SettingKey
import services.settings.SettingsService
import utils.Logging

import scala.util.{Failure, Success, Try}

object LicenseService extends Logging {
  private[this] var licenseContent: Option[String] = None
  private[this] var license: Option[License] = None

  def readLicense() = {
    val content = SettingsService(SettingKey.LicenseContent)
    if (content.isEmpty) {
      log.warn(" ::: Database Flow Trial Edition started.")
      license = None
      licenseContent = None
    } else {
      license = parseLicense(content) match {
        case Success(lic) =>
          log.warn(s"Database Flow ${lic.edition.title}, registered to [${lic.name}] (${lic.email}).")
          licenseContent = Some(content)
          Some(lic)
        case Failure(x) =>
          log.warn(s"Unable to parse license [$content].", x)
          None
      }
    }
    log.warn(" - Head to http://localhost:4260 to get started!")
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
  def isPersonalEdition = license.exists(_.edition == LicenseEdition.Personal)
  def isTeamEdition = license.exists(_.edition == LicenseEdition.Team)
}
