package models.settings

case class Setting(key: SettingKey, value: String) {
  def isDefault = value == key.default
  override def toString = s"$key=$value"
}
