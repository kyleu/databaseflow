package models.audit

import enumeratum._

object AuditStatus extends Enum[AuditStatus] {
  case object Started extends AuditStatus
  case object OK extends AuditStatus
  case object Error extends AuditStatus

  override def values = findValues
}

sealed trait AuditStatus extends EnumEntry
