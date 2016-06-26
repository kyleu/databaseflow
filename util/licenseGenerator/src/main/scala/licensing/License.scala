package licensing

import java.util.UUID

object License {
  def fromString(str: String): License = {
    val split = str.split('|')
    split.lastOption.map(_.toInt) match {
      case Some(1) if split.length == 5 => // valid, no op
      case Some(1) => throw new IllegalStateException(s"Expected [5] parameters, observed [${split.length}].")
      case Some(x) => throw new IllegalStateException(s"Invalid license version [$x].")
      case None => throw new IllegalStateException("Empty license.")
    }

    val id = UUID.fromString(split.headOption.getOrElse(throw new IllegalStateException()))
    val user = split(1)
    val edition = LicenseEdition.Personal // TODO split(2)
    val issued = split(3).toLong
    val version = split(4).toInt
    License(id, user, edition, issued, version)
  }
}

case class License(
    id: UUID,
    user: String,
    edition: LicenseEdition,
    issued: Long,
    version: Int
) {
  override def toString = s"""$id|$user|$edition|$issued|$version"""
}
