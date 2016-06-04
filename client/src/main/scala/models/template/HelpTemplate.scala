package models.template

import utils.KeyboardShortcut

import scalatags.Text.all._

object HelpTemplate {
  def content() = {
    val (globalShortcuts, nonGlobalShortcuts) = KeyboardShortcut.values.partition(_.isGlobal)

    val content = div(id := "help-panel")(
      h4("Global Shortcuts"),
      ul(globalShortcuts.map(s => patternToString(s))),
      h4("Editor Shortcuts"),
      ul(nonGlobalShortcuts.map(s => patternToString(s)))
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.help -> "Database Flow Help")
    )
  }

  private[this] def patternToString(s: KeyboardShortcut) = {
    li(s.pattern + ": " + s)
  }
}
