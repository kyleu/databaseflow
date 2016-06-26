package licensing

import enumeratum._

sealed abstract class LicenseEdition(val id: String) extends EnumEntry {
  override def toString = id
}

object LicenseEdition extends Enum[LicenseEdition] {
  case object Personal extends LicenseEdition("personal")
  case object Team extends LicenseEdition("team")

  override val values = findValues
}
