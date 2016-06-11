package models.audit

import enumeratum._

object AuditType extends Enum[AuditType] {
  case object SignIn extends AuditType
  case object SignOut extends AuditType
  case object CreateConnection extends AuditType
  case object EditConnection extends AuditType
  case object DeleteConnection extends AuditType
  case object Connect extends AuditType
  case object SaveQuery extends AuditType
  case object DeleteQuery extends AuditType
  case object Query extends AuditType
  case object Explain extends AuditType
  case object Analyze extends AuditType
  case object Execute extends AuditType

  override def values = findValues
}

sealed trait AuditType extends EnumEntry
