package utils

import java.util.UUID

import enumeratum._
import ui.EditorManager

sealed abstract class KeyboardShortcut(val pattern: String, val call: (Option[UUID]) => Unit, val isGlobal: Boolean = false) extends EnumEntry

object KeyboardShortcut extends Enum[KeyboardShortcut] {
  case object Save extends KeyboardShortcut("mod+s", {
    case Some(uuid) => EditorManager.onSave(uuid)
    case None => throw new IllegalStateException()
  })

  case object Run extends KeyboardShortcut("mod+enter", {
    case Some(uuid) => EditorManager.onRun(uuid)
    case None => throw new IllegalStateException()
  })

  case object Help extends KeyboardShortcut("?", {
    case Some(_) => throw new IllegalStateException()
    case None => utils.Logging.info("Loading help...")
  }, isGlobal = true)

  case object RefreshSchema extends KeyboardShortcut("r", {
    case Some(_) => throw new IllegalStateException()
    case None => utils.Logging.info("Refreshing schema...")
  }, isGlobal = true)

  case object NewQuery extends KeyboardShortcut("+", {
    case Some(_) => throw new IllegalStateException()
    case None => utils.Logging.info("Adding new query...")
  }, isGlobal = true)

  override val values = findValues
}
