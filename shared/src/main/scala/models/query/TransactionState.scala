package models.query

import enumeratum._

sealed trait TransactionState extends EnumEntry

object TransactionState extends Enum[TransactionState] {
  case object NotStarted extends TransactionState
  case object Started extends TransactionState
  case object Running extends TransactionState
  case object RolledBack extends TransactionState
  case object Committed extends TransactionState

  override val values = findValues
}
