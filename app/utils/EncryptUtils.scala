package utils

import play.api.libs.Crypto

object EncryptUtils {
  def encrypt(s: String) = s //Crypto.encryptAES(s)
  def decrypt(s: String) = s //Crypto.decryptAES(s)
}
