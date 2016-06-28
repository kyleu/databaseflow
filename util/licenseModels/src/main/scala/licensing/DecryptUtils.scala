package licensing

import xyz.wiedenhoeft.scalacrypt._

import scala.util.{Failure, Success}

object DecryptUtils {
  private[this] lazy val decryptSuite = suites.RSAES_OAEP(DecryptKeyProvider.decryptKey) match {
    case Success(suite) => suite
    case Failure(x) => throw x
  }

  def decrypt(encrypted: Seq[Byte]) = {
    val ret = decryptSuite.decrypt(encrypted) match {
      case Success(suite) => suite
      case Failure(x) => throw x
    }
    ret.map(_.toChar).mkString
  }
}
