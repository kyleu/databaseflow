package services.settings

import models.queries.settings.SettingQueries
import models.settings.{Setting, SettingKey}
import services.database.MasterDatabase

object SettingsService {
  private[this] var settings = Seq.empty[Setting]
  private[this] var settingsMap = Map.empty[SettingKey, String]

  def apply(key: SettingKey) = settingsMap.getOrElse(key, key.default)
  def asBool(key: SettingKey) = apply(key) == "true"

  def load() = {
    settingsMap = MasterDatabase.conn.query(SettingQueries.getAll()).map(s => s.key -> s.value).toMap
    settings = SettingKey.values.map(k => Setting(k, settingsMap.getOrElse(k, k.default)))
  }

  def isOverride(key: SettingKey) = settingsMap.isDefinedAt(key)

  def getAll = settings

  def set(key: SettingKey, value: String) = {
    val s = Setting(key, value)
    if (s.isDefault) {
      settingsMap = settingsMap - key
      MasterDatabase.conn.executeUpdate(SettingQueries.removeById(key))
    } else {
      MasterDatabase.conn.transaction { t =>
        t.query(SettingQueries.getById(key)) match {
          case Some(setting) => t.executeUpdate(SettingQueries.Update(s))
          case None => t.executeUpdate(SettingQueries.insert(s))
        }
      }
      settingsMap = settingsMap + (key -> value)
    }
    settings = SettingKey.values.map(k => Setting(k, settingsMap.getOrElse(k, k.default)))
  }

  def allowRegistration = asBool(SettingKey.AllowRegistration)
  def allowAuditRemoval = asBool(SettingKey.AllowAuditRemoval)
}
