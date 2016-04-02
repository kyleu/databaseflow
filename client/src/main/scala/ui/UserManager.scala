package ui

import java.util.UUID

import models.user.UserPreferences

object UserManager {
  var userId: Option[UUID] = None
  var username: Option[String] = None
  var preferences: Option[UserPreferences] = None
}
