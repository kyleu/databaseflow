package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import org.joda.time.LocalDateTime
import utils.DateUtils

object User {
  val mock = User(UUID.fromString("11111111-1111-1111-1111-111111111111"), Some("Test User"), UserPreferences.empty, LoginInfo("anonymous", "guest"))

  val personalEditionAdmin = User(
    UUID.fromString("00000000-0000-0000-0000-000000000000"),
    Some("Personal Edition"),
    UserPreferences.empty,
    LoginInfo("anonymous", "admin"),
    Set(Role.User, Role.Admin)
  )
}

case class User(
    id: UUID,
    username: Option[String] = None,
    preferences: UserPreferences,
    profile: LoginInfo,
    roles: Set[Role] = Set(Role.User),
    created: LocalDateTime = DateUtils.now
) extends Identity {
  def isAdmin = roles.contains(models.user.Role.Admin)
}
