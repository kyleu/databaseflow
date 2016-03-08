package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import org.joda.time.LocalDateTime
import utils.DateUtils

object User {
  val mock = User(UUID.fromString("11111111-1111-1111-1111-111111111111"), Some("Test User"), UserPreferences(), Seq.empty)
}

case class User(
    id: UUID,
    username: Option[String],
    preferences: UserPreferences,
    profiles: Seq[LoginInfo],
    roles: Set[Role] = Set(Role.User),
    created: LocalDateTime = DateUtils.now
) extends Identity {
  def isGuest = profiles.isEmpty
  def isAdmin = roles.contains(models.user.Role.Admin)
}
