package models.settings

import enumeratum._

sealed abstract class SettingKey(
    val id: String,
    val title: String,
    val default: String
) extends EnumEntry {
  override def toString = id
}

object SettingKey extends Enum[SettingKey] {
  case object SandboxA extends SettingKey("sandbox-a", "Sandbox A", "a")
  case object SandboxB extends SettingKey("sandbox-b", "Sandbox B", "b")
  case object SandboxC extends SettingKey("sandbox-c", "Sandbox C", "c")

  override val values = findValues
}
