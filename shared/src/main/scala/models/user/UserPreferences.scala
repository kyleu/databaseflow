package models.user

final case class UserPreferences(
  avatar: Option[String] = None,
  theme: String = "blue-grey"
)
