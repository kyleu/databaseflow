package models.settings

import enumeratum._
import services.database.MasterDatabase

sealed abstract class SettingKey(val id: String, val title: String, val description: String, val default: String) extends EnumEntry {
  override def toString = id
}

object SettingKey extends Enum[SettingKey] {
  case object AllowRegistration extends SettingKey(
    id = "allow-registration",
    title = "Allow Registration",
    description = "Determines if users are allowed to sign themselves up for the system.",
    default = "true"
  )

  case object DefaultNewUserRole extends SettingKey(
    id = "default-new-user-role",
    title = "Default New User Role",
    description = "Determines the role to assign newly-registered users.",
    default = "user"
  )

  case object AddConnectionRole extends SettingKey(
    id = "add-connection-role",
    title = "Add Connection Role",
    description = "Determines the role required to create new connections.",
    default = "visitor"
  )

  case object AllowAuditRemoval extends SettingKey(
    id = "allow-audit-removal",
    title = "Allow Audit Removal",
    description = "Determines if users are allowed to remove audit records from the system.",
    default = "true"
  )

  case object LicenseContent extends SettingKey(
    id = "license-content",
    title = "License Content",
    description = "Contains the software license content.",
    default = ""
  )

  override val values = findValues
}
