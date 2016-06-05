package utils

import java.util.UUID

import enumeratum._
import ui.metadata.MetadataManager
import ui.query.AdHocQueryManager
import ui.{ EditorManager, HelpManager, TabManager }

sealed abstract class KeyboardShortcut(val pattern: String, val call: (Option[UUID]) => Unit, val isGlobal: Boolean = true) extends EnumEntry

object KeyboardShortcut extends Enum[KeyboardShortcut] {
  case object Save extends KeyboardShortcut("mod+s", { uuid =>
    EditorManager.onSave(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Run extends KeyboardShortcut("mod+enter", { uuid =>
    EditorManager.onRun(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Help extends KeyboardShortcut("?", { _ =>
    HelpManager.show()
  })

  case object RefreshSchema extends KeyboardShortcut("r", { _ =>
    MetadataManager.refreshSchema()
  })

  case object NewQuery extends KeyboardShortcut("+", { _ =>
    AdHocQueryManager.addNewQuery()
  })

  case object CloseTab extends KeyboardShortcut("del", { _ =>
    utils.Logging.info("Closing tab...")
  })

  case object NextTab extends KeyboardShortcut("]", { _ =>
    TabManager.selectNextTab()
  })

  case object PreviousTab extends KeyboardShortcut("[", { _ =>
    TabManager.selectPreviousTab()
  })

  override val values = findValues
}
