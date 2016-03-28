package models.user

object UserPreferences {
  val empty = UserPreferences()
}

case class UserPreferences(
  avatar: Option[String] = None,
  theme: String = "blue-grey",
  editorTheme: String = "",
  tabSize: Int = 2,
  lowercaseIdentifiers: Boolean = true
)
