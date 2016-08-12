package licensing

import java.util.UUID

import scala.util.Try

object License {
  def fromString(str: String) = Try {
    val split = str.split('|')
    split.lastOption.map(_.toInt) match {
      case Some(1) if split.length == 6 => // valid, no op
      case Some(1) => throw new IllegalStateException(s"Expected [6] parameters, observed [${split.length}].")
      case Some(x) => throw new IllegalStateException(s"Invalid license version [$x].")
      case None => throw new IllegalStateException("Empty license.")
    }

    val id = UUID.fromString(split.headOption.getOrElse(throw new IllegalStateException()))
    val name = split(1)
    val email = split(2)
    val edition = LicenseEdition.withName(split(3))
    val issued = split(4).toLong
    val version = split(5).toInt
    License(id, name, email, edition, issued, version)
  }
}

case class License(
    id: UUID = UUID.randomUUID,
    name: String,
    email: String,
    edition: LicenseEdition = LicenseEdition.NonCommercial,
    issued: Long = System.currentTimeMillis,
    version: Int = 1
) {
  override def toString = productIterator.toSeq.mkString("|")
}
