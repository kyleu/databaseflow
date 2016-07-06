import java.util.UUID

import licensing._

object Entrypoint extends App {
  println("License Generator")

  val uuid = UUID.fromString("99999999-9999-9999-9999-999999999999")

  //KeyProvider.generateKeys()

  //println(KeyProvider.decryptKey.isPrivateKey + ", " + KeyProvider.encryptKey.isPublicKey)

  //val encrypted = EncryptUtils.encrypt("test")
  //val decrypted = EncryptUtils.decrypt(encrypted)
  //println("Decrypted: " + decrypted)

  val l = License(
    id = uuid,
    name = "Kyle",
    email = "kyle@databaseflow.com"
  )

  LicenseGenerator.saveLicense(l, overwrite = true)
  LicenseGenerator.listLicenses().foreach(println)

  println(LicenseGenerator.loadLicense(uuid))
}
