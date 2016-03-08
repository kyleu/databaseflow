package models.user

case class UserPreferences(
  avatar: Option[String] = None,
  theme: String = "blue-grey"
)
