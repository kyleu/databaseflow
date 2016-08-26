package models.user

import java.util.UUID

import enumeratum._

sealed abstract class Role(override val entryName: String) extends EnumEntry {
  def qualifies(target: Role): Boolean
  override def toString = entryName
}

object Role extends Enum[Role] {
  def apply(role: String): Role = Role.withName(role)
  def unapply(role: Role): Option[String] = Some(role.toString)

  override val values = findValues

  object Admin extends Role("admin") {
    override def qualifies(target: Role) = true
  }
  object User extends Role("user") {
    override def qualifies(target: Role) = target == Role.User || target == Role.Visitor
  }
  object Visitor extends Role("visitor") {
    override def qualifies(target: Role) = target == Role.Visitor
  }

  def matchPermissions(user: User, owner: UUID, model: String, perm: String, value: String) = {
    if (user.id == owner) {
      true -> s"You are the owner of this $model."
    } else {
      value match {
        case "admin" => if (user.role == Role.Admin) {
          true -> s"Administrators may $perm this $model."
        } else {
          false -> s"Only administrators are allowed to $perm this $model."
        }
        case "user" => if (user.role == Role.Admin || user.role == Role.User) {
          true -> s"All normal users may $perm this $model."
        } else {
          false -> s"Visitors are not allowed to $perm this $model."
        }
        case "visitor" => true -> s"All users, including visitors, may $perm this $model."
        case "private" => false -> s"Only the owner of this $model may $perm it."
        case x => false -> x
      }
    }
  }
}
