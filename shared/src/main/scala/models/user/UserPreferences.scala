package models.user

import models.template.Theme

object UserPreferences {
  val empty = UserPreferences()
}

case class UserPreferences(
  avatar: Option[String] = None,
  theme: Theme = Theme.BlueGrey,
  editorTheme: String = "",
  tabSize: Int = 2,
  lowercaseIdentifiers: Boolean = true
)
