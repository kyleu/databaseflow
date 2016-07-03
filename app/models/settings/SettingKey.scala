package models.settings

import enumeratum._
import models.schema.ColumnType
import models.schema.ColumnType._
import services.database.MasterDatabase

sealed abstract class SettingKey(
    val id: String,
    val title: String,
    val description: String,
    val keyType: ColumnType = BooleanType,
    val default: String = "true"
) extends EnumEntry {
  override def toString = id
}

object SettingKey extends Enum[SettingKey] {
  case object AllowAuditRemoval extends SettingKey(
    id = "allow-audit-removal",
    title = "Allow Audit Removal",
    description = "Determines if users are allowed to remove audit records from the system."
  )

  case object LicenseContent extends SettingKey(
    id = "license-content",
    title = "License Content",
    description = "Contains the software license content."
  )

  case object QueryCacheConnection extends SettingKey(
    id = "query-cache-connection",
    title = "Query Cache Connection",
    description = "Connection used for query caching (H2 or PostgreSQL only).",
    keyType = StringType,
    default = MasterDatabase.connectionId.toString
  )

  override val values = findValues
}
