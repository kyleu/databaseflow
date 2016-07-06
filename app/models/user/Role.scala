package models.user

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
}
