package utils

import play.api.libs.Crypto

object EncryptUtils {
  def encrypt(s: String) = Crypto.encryptAES(s)
  def decrypt(s: String) = Crypto.decryptAES(s)
}
