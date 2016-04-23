package services.settings

import models.queries.settings.SettingQueries
import models.settings.{ Setting, SettingKey }
import services.database.MasterDatabase

@javax.inject.Singleton
class SettingsService @javax.inject.Inject() () {
  private[this] var settings = Map.empty[SettingKey, String]

  def apply(key: SettingKey) = settings.getOrElse(key, key.default)

  def load() = settings = MasterDatabase.conn.query(SettingQueries.getAll()).map(s => s.key -> s.value).toMap

  def isOverride(key: SettingKey) = settings.isDefinedAt(key)

  def set(key: SettingKey, value: String) = {
    MasterDatabase.conn.transaction { t =>
      t.query(SettingQueries.getById(key)) match {
        case Some(setting) => t.executeUpdate(SettingQueries.Update(Setting(key, value)))
        case None => t.executeUpdate(SettingQueries.insert(Setting(key, value)))
      }
    }
    settings = settings + (key -> value)
  }
}
