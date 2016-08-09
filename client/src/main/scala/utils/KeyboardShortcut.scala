package utils

import java.util.UUID

import enumeratum._
import ui.metadata.MetadataManager
import ui.query.{AdHocQueryManager, QueryManager, SqlManager}
import ui.search.SearchManager
import ui.{EditorManager, HelpManager, TabManager}

sealed abstract class KeyboardShortcut(val pattern: String, val key: String, val call: (Option[UUID]) => Unit, val isGlobal: Boolean = true) extends EnumEntry

object KeyboardShortcut extends Enum[KeyboardShortcut] {
  case object Save extends KeyboardShortcut("mod+s", "save.query", { uuid =>
    EditorManager.onSave(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Run extends KeyboardShortcut("mod+enter", "run.active.query", { uuid =>
    EditorManager.onRun(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object RunAll extends KeyboardShortcut("shift+mod+enter", "run.all.queries", { uuid =>
    EditorManager.onRunAll(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Blur extends KeyboardShortcut("esc", "leave.editor", { uuid =>
    SqlManager.blurEditor(uuid.getOrElse(throw new IllegalStateException()))
  }, isGlobal = false)

  case object Help extends KeyboardShortcut("?", "help", { _ =>
    HelpManager.show()
  })

  case object Search extends KeyboardShortcut("/", "search", { _ =>
    SearchManager.focus()
  })

  case object RefreshSchema extends KeyboardShortcut("r", "refresh.schema", { _ =>
    MetadataManager.refreshSchema()
  })

  case object NewQuery extends KeyboardShortcut("+", "new.query", { _ =>
    AdHocQueryManager.addNewQuery()
  })

  case object CloseTab extends KeyboardShortcut("del", "close.tab", { _ =>
    TabManager.getActiveTab.foreach { id =>
      QueryManager.closeQuery(id)
    }
  })

  case object NextTab extends KeyboardShortcut("]", "next.tab", { _ =>
    TabManager.selectNextTab()
  })

  case object PreviousTab extends KeyboardShortcut("[", "previous.tab", { _ =>
    TabManager.selectPreviousTab()
  })

  override val values = findValues
}
