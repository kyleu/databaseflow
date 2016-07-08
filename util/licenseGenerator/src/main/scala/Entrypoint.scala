import java.util.UUID

import licensing._

object Entrypoint extends App {
  val uuid = UUID.fromString("99999999-9999-9999-9999-999999999999")

  //KeyProvider.generateKeys()

  //val encrypted = EncryptUtils.encrypt("test")
  //val decrypted = EncryptUtils.decrypt(encrypted)

  val l = License(
    id = uuid,
    name = "Kyle",
    email = "kyle@databaseflow.com"
  )

  //LicenseGenerator.saveLicense(l, overwrite = true)
  //LicenseGenerator.listLicenses()
}
