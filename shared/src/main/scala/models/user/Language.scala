package models.user

import enumeratum.{Enum, EnumEntry}

sealed abstract class Language(val code: String) extends EnumEntry

object Language extends Enum[Language] {
  case object English extends Language("en")
  case object Arabic extends Language("ar")
  case object German extends Language("de")
  case object Spanish extends Language("es")
  case object French extends Language("fr")
  case object Hindi extends Language("hi")
  case object Japanese extends Language("ja")
  case object Portugeuse extends Language("pt")
  case object Chinese extends Language("zh")

  override def values = findValues
}
