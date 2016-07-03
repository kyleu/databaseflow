package services.licensing

import java.nio.file.{Files, Paths}
import java.util.Base64

import licensing.{DecryptUtils, License, LicenseEdition}
import utils.Logging

object LicenseService extends Logging {
  private[this] val licenseDir = "./conf"
  private[this] val licenseFilename = "databaseflow.license"

  private[this] var licenseContent: Option[String] = None
  private[this] var license: Option[License] = None

  def readLicense() = {
    val file = Paths.get(licenseDir, licenseFilename)
    if (Files.exists(file)) {
      val content = Files.readAllBytes(file)
      licenseContent = Some(new String(content))
      val decoded = Base64.getDecoder.decode(content)
      val decrypted = DecryptUtils.decrypt(decoded)
      val l = License.fromString(decrypted)
      log.info(s" ::: Database Flow ${l.edition.title}, registered to [${l.user}].")
      license = Some(l)
    } else {
      log.info(" ::: Database Flow Trial Edition.")
    }
  }

  def getLicense = license
  def getLicenseContent = licenseContent
  def hasLicense = license.isDefined
  def isPersonalEdition = license.exists(_.edition == LicenseEdition.Personal)
  def isTeamEdition = license.exists(_.edition == LicenseEdition.Team)
}
