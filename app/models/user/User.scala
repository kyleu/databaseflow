package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import org.joda.time.LocalDateTime
import utils.DateUtils

object User {
  val mock = User(UUID.fromString("11111111-1111-1111-1111-111111111111"), Some("Test User"), UserPreferences.empty, LoginInfo("anonymous", "guest"))
}

case class User(
    id: UUID,
    username: Option[String] = None,
    preferences: UserPreferences,
    profile: LoginInfo,
    role: Role = Role.Visitor,
    created: LocalDateTime = DateUtils.now
) extends Identity {
  def isAdmin = role == Role.Admin
}
