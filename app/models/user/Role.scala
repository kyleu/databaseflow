package models.user

import com.mohiva.play.silhouette.api.{ Authenticator, Authorization }
import enumeratum._
import play.api.i18n._
import play.api.mvc.Request

import scala.concurrent.Future

final case class WithRole(role: Role) extends Authorization[User, Authenticator] {
  override def isAuthorized[B](user: User, authenticator: Authenticator)(implicit request: Request[B], messages: Messages) = {
    Future.successful(user.roles match {
      case list: Set[Role] => list.contains(role)
      case _ => false
    })
  }
}

sealed abstract class Role(override val entryName: String) extends EnumEntry {
  override def toString = entryName
}

object Role extends Enum[Role] {
  def apply(role: String): Role = Role.withName(role)
  def unapply(role: Role): Option[String] = Some(role.toString)

  override def values = findValues

  object Admin extends Role("admin")
  object User extends Role("user")
  object Unknown extends Role("unknown")
}
