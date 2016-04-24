import java.nio.file.{ Files, Paths }

import net.nicholaswilliams.java.licensing._
import net.nicholaswilliams.java.licensing.encryption.{ PasswordProvider, PrivateKeyDataProvider, PublicKeyDataProvider, RSAKeyPairGenerator }
import net.nicholaswilliams.java.licensing.licensor.{ LicenseCreator, LicenseCreatorProperties }
import org.apache.commons.codec.binary.Base64

object LicenseGenerator extends App {
  private[this] val keyDir = "./util/licenseGenerator/src/main/resources/"

  println("License Generator")

  val privateKeyFilename = "license-private.key"
  val privateKeyPassword = "igotmadmadflow_private".toCharArray

  val publicKeyFilename = "license-public.key"
  val publicKeyPassword = "igotmadmadflow".toCharArray

  val licensePassword = "dbdbdbflow".toCharArray
  val testLicenseFilename = "kyle@databaseflow.com.license"

  //generateKeys()
  readPrivateKeys()
  readPublicKeys()
  //generateLicense("team", "kyle@databaseflow.com")
  writeTestLicense()
  //readTestLicense()

  def generateKeys() = {
    val generator = new RSAKeyPairGenerator()
    val keyPair = generator.generateKeyPair()
    generator.saveKeyPairToFiles(
      keyPair,
      keyDir + privateKeyFilename,
      keyDir + publicKeyFilename,
      privateKeyPassword,
      publicKeyPassword
    )
  }

  def readPrivateKeys() = {
    LicenseCreatorProperties.setPrivateKeyDataProvider(new PrivateKeyDataProvider {
      override def getEncryptedPrivateKeyData = Files.readAllBytes(Paths.get(keyDir, privateKeyFilename))
    })
    LicenseCreatorProperties.setPrivateKeyPasswordProvider(new PasswordProvider {
      override def getPassword = privateKeyPassword
    })
    LicenseCreator.getInstance()
  }

  def readPublicKeys() = {
    LicenseManagerProperties.setPublicKeyDataProvider(new PublicKeyDataProvider {
      override def getEncryptedPublicKeyData = Files.readAllBytes(Paths.get(keyDir, publicKeyFilename))
    })
    LicenseManagerProperties.setPublicKeyPasswordProvider(new PasswordProvider {
      override def getPassword = publicKeyPassword
    })
    LicenseManagerProperties.setLicenseProvider(new LicenseProvider {
      override def getLicense(context: scala.Any) = {
        val encodedLicenseData = Files.readAllBytes(Paths.get(keyDir, testLicenseFilename))
        val decodedLicenseData = Base64.decodeBase64(encodedLicenseData)

        val publicKeyData = Files.readAllBytes(Paths.get(keyDir, publicKeyFilename))
        new SignedLicense(decodedLicenseData, publicKeyData)
      }
    })
    LicenseManagerProperties.setLicensePasswordProvider(new PasswordProvider {
      override def getPassword = licensePassword
    })

    LicenseManagerProperties.setLicenseValidator(new LicenseValidator {
      override def validateLicense(license: License) = {}
    })

    LicenseManagerProperties.setCacheTimeInMinutes(720)

    LicenseManager.getInstance()
  }

  def generateLicense(edition: String, licensee: String) = {
    val license = new License.Builder()
      //.withProductKey("DATA-BASE-FLOW-LAX7-TN31-1TAX")
      .withHolder(licensee)
      .addFeature(edition)
      .build()

    LicenseCreator.getInstance().signAndSerializeLicense(license, licensePassword)
  }

  def writeTestLicense() = {
    val bytes = generateLicense("team", "kyle@databaseflow.com")
    val encodedLicenseData = Base64.encodeBase64(bytes)
    Files.write(Paths.get(keyDir, testLicenseFilename), encodedLicenseData)
  }

  def readTestLicense() = {
    val encodedLicenseData = Files.readAllBytes(Paths.get(keyDir, testLicenseFilename))
    val decodedLicenseData = Base64.decodeBase64(encodedLicenseData)

    val manager = LicenseManager.getInstance()

    manager.getLicense("databaseflow")
  }
}
