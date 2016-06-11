package utils

import enumeratum._

sealed abstract class TipsAndTricks(val content: String) extends EnumEntry

object TipsAndTricks extends Enum[TipsAndTricks] {
  case object NewQuery extends TipsAndTricks("Press \"+\" when outside of a sql editor to start a new query.")
  case object SwitchTabs extends TipsAndTricks("Press \"[\" or \"]\" when outside of a sql editor to switch between tabs.")
  case object OpenInNewTab extends TipsAndTricks("Almost all the links in Database Flow can be opened in new tabs for easy side-by-side comparisons.")

  override val values = findValues
}

