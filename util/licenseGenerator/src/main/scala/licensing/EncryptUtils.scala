package licensing

import xyz.wiedenhoeft.scalacrypt._

import scala.util.{Failure, Success}

object EncryptUtils {
  private[this] lazy val encryptSuite = suites.RSAES_OAEP(EncryptKeyProvider.encryptKey) match {
    case Success(suite) => suite
    case Failure(x) => throw x
  }

  def encrypt(s: String) = encryptSuite.encrypt(s.getBytes) match {
    case Success(suite) => suite
    case Failure(x) => throw x
  }
}
