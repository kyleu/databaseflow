import net.nicholaswilliams.java.licensing._
import net.nicholaswilliams.java.licensing.encryption.{ PasswordProvider, PrivateKeyDataProvider, PublicKeyDataProvider, RSAKeyPairGenerator }
import net.nicholaswilliams.java.licensing.licensor.LicenseCreatorProperties
import org.apache.commons.io.IOUtils

object KeyGenerator {
  val privateKeyFilename = "license-private.key"
  val privateKeyPassword = "igotmadmadflow_private".toCharArray
  val privateKeyData = IOUtils.toByteArray(this.getClass.getResourceAsStream("keys/" + privateKeyFilename))

  val publicKeyFilename = "license-public.key"
  val publicKeyPassword = "igotmadmadflow".toCharArray
  val publicKeyData = IOUtils.toByteArray(this.getClass.getResourceAsStream("keys/" + publicKeyFilename))

  def init() = {
    LicenseCreatorProperties.setPrivateKeyDataProvider(new PrivateKeyDataProvider {
      override def getEncryptedPrivateKeyData = privateKeyData
    })
    LicenseCreatorProperties.setPrivateKeyPasswordProvider(new PasswordProvider {
      override def getPassword = privateKeyPassword
    })

    LicenseManagerProperties.setPublicKeyDataProvider(new PublicKeyDataProvider {
      override def getEncryptedPublicKeyData = publicKeyData
    })
    LicenseManagerProperties.setPublicKeyPasswordProvider(new PasswordProvider {
      override def getPassword = publicKeyPassword
    })
  }

  def generateKeys() = {
    val generator = new RSAKeyPairGenerator()
    val keyDir = "./util/licenseGenerator/src/main/resources/keys/"
    val keyPair = generator.generateKeyPair()
    generator.saveKeyPairToFiles(
      keyPair,
      keyDir + privateKeyFilename,
      keyDir + publicKeyFilename,
      privateKeyPassword,
      publicKeyPassword
    )
  }
}
