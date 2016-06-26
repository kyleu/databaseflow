package licensing

import xyz.wiedenhoeft.scalacrypt._

object EncryptUtils {
  private[this] lazy val encryptSuite = suites.RSAES_OAEP(EncryptKeyProvider.encryptKey).get

  def encrypt(s: String) = {
    encryptSuite.encrypt(s.getBytes).get
  }
}
