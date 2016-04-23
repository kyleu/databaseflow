package models.settings

import enumeratum._

sealed abstract class SettingKey(
    val id: String,
    val default: String
) extends EnumEntry {
  override def toString = id
}

object SettingKey extends Enum[SettingKey] {
  case object SandboxA extends SettingKey("sandbox-a", "a")
  case object SandboxB extends SettingKey("sandbox-b", "b")
  case object SandboxC extends SettingKey("sandbox-c", "c")

  override val values = findValues
}
