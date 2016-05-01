package models.settings

import enumeratum._
import models.schema.ColumnType
import models.schema.ColumnType.{ BooleanType, IntegerType, StringType }

sealed abstract class SettingKey(
    val id: String,
    val title: String,
    val description: String,
    val keyType: ColumnType,
    val default: String
) extends EnumEntry {
  override def toString = id
}

object SettingKey extends Enum[SettingKey] {
  case object SandboxA extends SettingKey(
    id = "sandbox-a",
    title = "Sandbox A",
    description = "Set a string value for A.",
    keyType = StringType,
    default = "String Value"
  )
  case object SandboxB extends SettingKey(
    id = "sandbox-b",
    title = "Sandbox B",
    description = "Set an integer value for B.",
    keyType = IntegerType,
    default = "1234"
  )
  case object SandboxC extends SettingKey(
    id = "sandbox-c",
    title = "Sandbox C",
    description = "Set a boolean value for C.",
    keyType = BooleanType,
    default = "false"
  )

  override val values = findValues
}
