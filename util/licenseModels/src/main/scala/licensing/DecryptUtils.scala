package licensing

import xyz.wiedenhoeft.scalacrypt._

object DecryptUtils {
  private[this] lazy val decryptSuite = suites.RSAES_OAEP(DecryptKeyProvider.decryptKey).get

  def decrypt(encrypted: Seq[Byte]) = {
    decryptSuite.decrypt(encrypted).get.map(_.toChar).mkString
  }
}
