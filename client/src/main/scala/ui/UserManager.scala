package ui

import java.util.UUID

import models.UserSettings
import models.user.UserPreferences

object UserManager {
  var userId: Option[UUID] = None
  var username: Option[String] = None
  var email: Option[String] = None
  var preferences: Option[UserPreferences] = None
  val rowsReturned = 100

  def onUserSettings(us: UserSettings) = {
    userId = us.userId
    username = us.username
    email = us.email
    preferences = us.preferences
  }
}
