package utils

import enumeratum._

sealed abstract class TipsAndTricks(val content: String) extends EnumEntry

object TipsAndTricks extends Enum[TipsAndTricks] {
  case object ProfileTheme extends TipsAndTricks("You can change the color scheme of Database Flow from your profile once signed in.")
  case object SearchHotkey extends TipsAndTricks("Press \"/\" or click in the search box to start filtering database objects.")
  case object NewQuery extends TipsAndTricks("Press \"+\" when outside of a sql editor to start a new query.")
  case object SwitchTabs extends TipsAndTricks("Press \"[\" or \"]\" when outside of a sql editor to switch between tabs.")
  case object OpenInNewTab extends TipsAndTricks("Almost all the links in Database Flow can be opened in new browser tabs for easy side-by-side comparisons.")

  override val values = findValues
}

