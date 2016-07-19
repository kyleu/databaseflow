package utils

import java.util.UUID

import enumeratum._
import ui.metadata.MetadataManager
import ui.query.{AdHocQueryManager, QueryManager, SqlManager}
import ui.search.SearchManager
import ui.{EditorManager, HelpManager, TabManager}

sealed abstract class KeyboardShortcut(val pattern: String, val desc: String, val call: (Option[UUID]) => Unit, val isGlobal: Boolean = true) extends EnumEntry

object KeyboardShortcut extends Enum[KeyboardShortcut] {
  case object Save extends KeyboardShortcut("mod+s", "Save Query", { uuid =>
    EditorManager.onSave(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Run extends KeyboardShortcut("mod+enter", "Run Query", { uuid =>
    EditorManager.onRun(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Blur extends KeyboardShortcut("esc", "Leave Editor", { uuid =>
    SqlManager.blurEditor(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Help extends KeyboardShortcut("?", "Help", { _ =>
    HelpManager.show()
  })

  case object Search extends KeyboardShortcut("/", "Search", { _ =>
    SearchManager.focus()
  })

  case object RefreshSchema extends KeyboardShortcut("r", "Refresh Schema", { _ =>
    MetadataManager.refreshSchema()
  })

  case object NewQuery extends KeyboardShortcut("+", "New Query", { _ =>
    AdHocQueryManager.addNewQuery()
  })

  case object CloseTab extends KeyboardShortcut("del", "Close Tab", { _ =>
    TabManager.getActiveTab.foreach { id =>
      QueryManager.closeQuery(id)
    }
  })

  case object NextTab extends KeyboardShortcut("]", "Select Next Tab", { _ =>
    TabManager.selectNextTab()
  })

  case object PreviousTab extends KeyboardShortcut("[", "Select Previous Tab", { _ =>
    TabManager.selectPreviousTab()
  })

  override val values = findValues
}
