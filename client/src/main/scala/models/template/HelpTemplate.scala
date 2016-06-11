package models.template

import utils.{ KeyboardShortcut, TipsAndTricks }

import scalatags.Text.all._

object HelpTemplate {
  def content() = {
    val (globalShortcuts, nonGlobalShortcuts) = KeyboardShortcut.values.partition(_.isGlobal)

    val content = div(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "z-depth-1 help-panel")(
            h5("Tips and Tricks"),
            div(id := "tip-detail")("Loading...")
          )
        )
      ),
      div(cls := "row")(
        div(cls := "col s12 m6")(
          div(cls := "z-depth-1 help-panel")(
            h5("Global Shortcuts"),
            ul(globalShortcuts.map(s => patternToString(s)))
          )
        ),
        div(cls := "col s12 m6")(
          div(cls := "z-depth-1 help-panel")(
            h5("Editor Shortcuts"),
            ul(nonGlobalShortcuts.map(s => patternToString(s)))
          )
        )
      )
    )
    StaticPanelTemplate.cardRow(content, iconAndTitle = Some(Icons.help -> "Database Flow Help"))
  }

  private[this] def patternToString(s: KeyboardShortcut) = {
    li(s.pattern + ": " + s)
  }
}
