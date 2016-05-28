package licensing

import xyz.wiedenhoeft.scalacrypt._

object EncryptUtils {
  private[this] lazy val decryptSuite = suites.RSAES_OAEP(KeyProvider.decryptKey).get
  private[this] lazy val encryptSuite = suites.RSAES_OAEP(KeyProvider.encryptKey).get

  def encrypt(s: String) = {
    encryptSuite.encrypt(s.getBytes).get
  }

  def decrypt(encrypted: Seq[Byte]) = {
    decryptSuite.decrypt(encrypted).get.map(_.toChar).mkString
  }
}
