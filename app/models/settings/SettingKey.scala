package models.settings

import enumeratum._
import models.schema.ColumnType
import models.schema.ColumnType._

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
  case object AllowGuests extends SettingKey(
    id = "allow-guests",
    title = "Allow Guests",
    description = "Determines if anonymous users are allowed.",
    keyType = BooleanType,
    default = "true"
  )

  override val values = findValues
}
