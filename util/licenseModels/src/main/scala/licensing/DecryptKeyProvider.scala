package licensing

import java.nio.file.{Files, Paths}
import java.util.Base64

import xyz.wiedenhoeft.scalacrypt._

object DecryptKeyProvider {
  val decryptKeyDir = "./util/licenseModels/src/main/resources/keys/"
  val decryptKeyFilename = "license-decrypt.key"
  lazy val decryptKey = {
    val decryptKeyContent = Files.readAllBytes(Paths.get(decryptKeyDir, decryptKeyFilename))
    val decryptKeyContentDecoded = Base64.getDecoder.decode(decryptKeyContent).toSeq
    decryptKeyContentDecoded.toKey[RSAKey].get
  }
}
