import keys.{ PrivateKeyProvider, PrivatePasswordProvider, PublicKeyProvider }
import net.nicholaswilliams.java.licensing._
import net.nicholaswilliams.java.licensing.encryption.RSAKeyPairGeneratorInterface.GeneratedClassDescriptor
import net.nicholaswilliams.java.licensing.encryption.{ PasswordProvider, PrivateKeyDataProvider, PublicKeyDataProvider, RSAKeyPairGenerator }
import net.nicholaswilliams.java.licensing.licensor.LicenseCreatorProperties
import org.apache.commons.io.IOUtils

object KeyGenerator {
  val keyPassword = "igotmadmadflow".toCharArray

  val privateKeyFilename = "license-private.key"
  //val privateKeyPassword = "igotmadmadflow_private".toCharArray
  val privateKeyData = IOUtils.toByteArray(this.getClass.getResourceAsStream("keys/" + privateKeyFilename))

  val publicKeyFilename = "license-public.key"
  //val publicKeyPassword = "igotmadmadflow".toCharArray
  val publicKeyData = IOUtils.toByteArray(this.getClass.getResourceAsStream("keys/" + publicKeyFilename))

  def init() = {
    LicenseCreatorProperties.setPrivateKeyDataProvider(PrivateKeyProvider)
    LicenseCreatorProperties.setPrivateKeyPasswordProvider(PrivatePasswordProvider)

    LicenseManagerProperties.setPublicKeyDataProvider(PublicKeyProvider)
    LicenseManagerProperties.setPublicKeyPasswordProvider(PrivatePasswordProvider)
  }

  def generateKeys() = {
    val generator = new RSAKeyPairGenerator()

    val keyPair = generator.generateKeyPair()

    val rkd = new GeneratedClassDescriptor().setPackageName("my.packagename").setClassName("PrivateKeyProvider")
    val ukd = new GeneratedClassDescriptor().setPackageName("my.packagename").setClassName("PublicKeyProvider")
    val pd = new GeneratedClassDescriptor().setPackageName("my.packagename").setClassName("PasswordProvider")

    generator.saveKeyPairToProviders(keyPair, rkd, ukd, keyPassword)
    generator.savePasswordToProvider(keyPassword, pd)

    println(rkd.getJavaFileContents + "\n\n" + ukd.getJavaFileContents + "\n\n" + pd.getJavaFileContents)
  }

  def generateKeysOld() = {
    val generator = new RSAKeyPairGenerator()
    val keyDir = "./util/licenseGenerator/src/main/resources/keys/"
    val keyPair = generator.generateKeyPair()
    generator.saveKeyPairToFiles(
      keyPair,
      keyDir + privateKeyFilename,
      keyDir + publicKeyFilename,
      keyPassword
    )
  }
}
