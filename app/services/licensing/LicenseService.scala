package services.licensing

import java.nio.file.{Files, Paths}
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
      log.info(" ::: Database Flow Trial Edition.")
      license = None
      licenseContent = None
    } else {
      val l = parseLicense(content) match {
        case Success(lic) => lic
        case Failure(x) => throw x
      }
      log.info(s" ::: Database Flow ${l.edition.title}, registered to [${l.user}].")
      license = Some(l)
      licenseContent = Some(content)
    }
  }

  def parseLicense(content: String) = Try {
    val decoded = Base64.getDecoder.decode(content)
    val decrypted = DecryptUtils.decrypt(decoded)
    License.fromString(decrypted)
  }

  def getLicense = license
  def getLicenseContent = licenseContent
  def hasLicense = license.isDefined
  def isPersonalEdition = license.exists(_.edition == LicenseEdition.Personal)
  def isTeamEdition = license.exists(_.edition == LicenseEdition.Team)
}
