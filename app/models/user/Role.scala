package models.user

import java.util.UUID

import enumeratum._

sealed abstract class Role(override val entryName: String) extends EnumEntry {
  override def toString = entryName
}

object Role extends Enum[Role] {
  def apply(role: String): Role = Role.withName(role)
  def unapply(role: Role): Option[String] = Some(role.toString)

  override def values = findValues

  object Admin extends Role("admin")
  object User extends Role("user")
  object Visitor extends Role("visitor")

  def matchPermissions(user: Option[User], owner: Option[UUID], model: String, perm: String, value: String) = user match {
    case Some(u) =>
      if (owner.contains(u.id)) {
        true -> "You are the owner of this connection."
      } else {
        value match {
          case "admin" => if (u.role == Role.Admin) {
            true -> s"Administrators may $perm this $model."
          } else {
            false -> s"Only administrators are allowed to $perm this $model."
          }
          case "user" => if (u.role == Role.Admin || u.role == Role.User) {
            true -> s"All normal users may $perm this $model."
          } else {
            false -> s"Visitors are not allowed to $perm this $model."
          }
          case "visitor" => true -> s"All users, including visitors, may $perm this $model."
          case "private" => if (owner.isDefined) {
            false -> s"Only the owner of this $model may $perm it."
          } else {
            true -> s"Anyone may $perm this connection."
          }
          case x => false -> x.toString
        }
      }
    case None =>
      if (owner.isEmpty || value == "visitor") {
        true -> s"Visitors are allowed to $perm this $model."
      } else {
        false -> s"Visitors are not allowed to $perm this $model."
      }
  }

}
