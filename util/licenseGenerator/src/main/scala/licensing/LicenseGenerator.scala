package licensing

import java.nio.file.{ Files, Paths }
import java.util.Base64

object LicenseGenerator {
  private[this] val licenseDir = "./util/licenseGenerator/src/main/resources/licenses/"

  def listLicenses() = {
    val dir = new java.io.File(licenseDir)
    dir.listFiles.filter(_.getName.endsWith(".license")).map(_.getName.stripSuffix(".license"))
  }

  def loadLicense(id: String) = {
    val filename = id + ".license"
    val file = Paths.get(licenseDir, filename)
    if (Files.exists(file)) {
      val content = Files.readAllBytes(file)
      val decoded = Base64.getDecoder.decode(content)
      val str = EncryptUtils.decrypt(decoded)
      License(str)
    } else {
      throw new IllegalArgumentException(s"License already exists for [$id].")
    }
  }

  def saveLicense(license: License, overwrite: Boolean = false) = {
    val filename = license.id + ".license"
    val file = Paths.get(licenseDir, filename)
    if ((!overwrite) && Files.exists(file)) {
      throw new IllegalArgumentException(s"License already exists for [${license.id}] and cannot be overwritten.")
    } else {
      val encrypted = EncryptUtils.encrypt(license.id).toArray
      val encoded = Base64.getEncoder.encode(encrypted)
      Files.write(file, encoded)
    }
  }

  def removeLicense(id: String) = {
    val filename = id + ".license"
    val file = Paths.get(licenseDir, filename)
    if (Files.exists(file)) {
      Files.delete(file)
    } else {
      throw new IllegalArgumentException(s"No license available for [$id].")
    }
  }
}
