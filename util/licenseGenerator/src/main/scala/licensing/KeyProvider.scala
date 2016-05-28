package licensing

import java.nio.file.{ Files, Paths }
import java.util.Base64

import xyz.wiedenhoeft.scalacrypt._

object KeyProvider {
  private[this] val keyDir = "./util/licenseGenerator/src/main/resources/keys/"

  private[this] val decryptKeyFilename = "license-decrypt.key"
  lazy val decryptKey = {
    val decryptKeyContent = Files.readAllBytes(Paths.get(keyDir, decryptKeyFilename))
    val decryptKeyContentDecoded = Base64.getDecoder.decode(decryptKeyContent).toSeq
    decryptKeyContentDecoded.toKey[RSAKey].get
  }

  private[this] val encryptKeyFilename = "license-encrypt.key"
  lazy val encryptKey = {
    val encryptKeyContent = Files.readAllBytes(Paths.get(keyDir, encryptKeyFilename))
    val encryptKeyContentDecoded = Base64.getDecoder.decode(encryptKeyContent).toSeq
    encryptKeyContentDecoded.toKey[RSAKey].get
  }

  def generateKeys() = {
    val privateKey = Key.generate[RSAKey]
    val privateKeyContent = Base64.getEncoder.encode(privateKey.bytes.toArray)
    val privateKeyContentDecoded = Base64.getDecoder.decode(privateKeyContent).toSeq
    val newPrivateKey = privateKeyContentDecoded.toKey[RSAKey]
    println("Private Key:")
    println(privateKeyContent.map(_.toChar).mkString)
    Files.write(Paths.get(keyDir, decryptKeyFilename), privateKeyContent)

    val publicKey = privateKey.publicKey
    val publicKeyContent = Base64.getEncoder.encode(publicKey.bytes.toArray)
    val publicKeyContentDecoded = Base64.getDecoder.decode(publicKeyContent).toSeq
    val newPublicKey = publicKeyContentDecoded.toKey[RSAKey]
    println("Public Key:")
    println(publicKeyContent.map(_.toChar).mkString)
    Files.write(Paths.get(keyDir, encryptKeyFilename), publicKeyContent)
  }
}
