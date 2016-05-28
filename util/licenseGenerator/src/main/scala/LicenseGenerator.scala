import java.nio.file.{ Files, Paths }

import keys.PublicKeyProvider
import net.nicholaswilliams.java.licensing._
import net.nicholaswilliams.java.licensing.encryption.PasswordProvider
import net.nicholaswilliams.java.licensing.licensor.LicenseCreator
import org.apache.commons.codec.binary.Base64

object LicenseGenerator {
  val licenseDir = "./util/licenseGenerator/src/main/resources/licenses"

  val licensePassword = "dbdbdbflow".toCharArray

  def init() = {
    LicenseManagerProperties.setLicenseProvider(new LicenseProvider {
      override def getLicense(context: scala.Any) = {
        val encodedLicenseData = Files.readAllBytes(Paths.get(LicenseGenerator.licenseDir, context.toString + ".license"))
        val decodedLicenseData = Base64.decodeBase64(encodedLicenseData)
        new SignedLicense(decodedLicenseData, PublicKeyProvider.getEncryptedPublicKeyData)
      }
    })
    LicenseManagerProperties.setLicensePasswordProvider(new PasswordProvider {
      override def getPassword = LicenseGenerator.licensePassword
    })

    LicenseManagerProperties.setLicenseValidator(new LicenseValidator {
      override def validateLicense(license: License) = {}
    })

    LicenseManagerProperties.setCacheTimeInMinutes(720)
  }

  def writeLicense(edition: String, licensee: String) = {
    val bytes = generateLicense(edition, licensee)
    val encodedLicenseData = Base64.encodeBase64(bytes)
    Files.write(Paths.get(licenseDir, licensee + ".license"), encodedLicenseData)
  }

  def generateLicense(edition: String, licensee: String) = {
    val license = new License.Builder()
      //.withProductKey("DATA-BASE-FLOW-LAX7-TN31-1TAX")
      .withHolder(licensee)
      //.addFeature(edition)
      .build()

    LicenseCreator.getInstance().signAndSerializeLicense(license, licensePassword)
  }

  def writeTestLicense() = writeLicense("team", "kyle@databaseflow.com")

  def readTestLicense() = LicenseManager.getInstance().getLicense("kyle@databaseflow.com")
}
