package util

import enumeratum._

sealed abstract class TipsAndTricks(val key: String) extends EnumEntry

object TipsAndTricks extends Enum[TipsAndTricks] with CirceEnum[TipsAndTricks] {
  case object ProfileTheme extends TipsAndTricks("theme")
  case object SearchHotkey extends TipsAndTricks("hotkey.search")
  case object NewQuery extends TipsAndTricks("hotkey.new")
  case object SwitchTabs extends TipsAndTricks("hotkey.switch")
  case object OpenInNewTab extends TipsAndTricks("new.tab")

  override val values = findValues
}

