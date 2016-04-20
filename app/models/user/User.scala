package models.user

import java.util.UUID

import org.joda.time.LocalDateTime
import utils.DateUtils

object User {
  val mock = User(UUID.fromString("11111111-1111-1111-1111-111111111111"), Some("Test User"), UserPreferences.empty, Seq.empty)
}

case class User(
    id: UUID,
    username: Option[String] = None,
    preferences: UserPreferences,
    // TODO profiles: Seq[LoginInfo],
    profiles: Seq[String],
    roles: Set[Role] = Set(Role.User),
    created: LocalDateTime = DateUtils.now
) {
  def isGuest = profiles.isEmpty
  def isAdmin = roles.contains(models.user.Role.Admin)
}
