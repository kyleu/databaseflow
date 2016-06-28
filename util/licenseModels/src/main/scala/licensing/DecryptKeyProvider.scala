package licensing

import java.util.Base64

import xyz.wiedenhoeft.scalacrypt._

import scala.util.{Failure, Success}

object DecryptKeyProvider {
  val decryptKeyFilename = "keys/license-decrypt.key"
  lazy val decryptKey = {
    val decryptKeyContent = {
      val f = this.getClass.getClassLoader.getResourceAsStream(decryptKeyFilename)
      Stream.continually(f.read()).takeWhile(_ != -1).map(_.toByte).toArray
    }
    val decryptKeyContentDecoded = Base64.getDecoder.decode(decryptKeyContent).toSeq
    decryptKeyContentDecoded.toKey[RSAKey] match {
      case Success(key) => key
      case Failure(x) => throw x
    }
  }
}
