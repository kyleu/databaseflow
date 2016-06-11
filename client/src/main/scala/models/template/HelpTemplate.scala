package models.template

import utils.KeyboardShortcut

import scalatags.Text.all._

object HelpTemplate {
  val content = {
    val (globalShortcuts, nonGlobalShortcuts) = KeyboardShortcut.values.partition(_.isGlobal)

    val content = div(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "z-depth-1 help-panel")(
            h5("Tips and Tricks"),
            div(id := "tip-detail")("Loading..."),
            div(
              div(style := "float: left;")(a(cls := "previous-tip-link", href := "")("Previous")),
              div(style := "float: right;")(a(cls := "next-tip-link", href := "")("Next")),
              div(style := "clear: both;")
            )
          )
        )
      ),
      div(cls := "row")(
        div(cls := "col s12 m6")(
          div(cls := "z-depth-1 help-panel")(
            h5("Global Shortcuts"),
            table(cls := "bordered highlight")(
              tbody(
                globalShortcuts.map(s => patternToRow(s))
              )
            )
          )
        ),
        div(cls := "col s12 m6")(
          div(cls := "z-depth-1 help-panel")(
            h5("Editor Shortcuts"),
            table(cls := "bordered highlight")(
              tbody(
                nonGlobalShortcuts.map(s => patternToRow(s))
              )
            )
          )
        )
      )
    )
    StaticPanelTemplate.cardRow(content, iconAndTitle = Some(Icons.help -> "Database Flow Help"))
  }

  private[this] def patternToRow(s: KeyboardShortcut) = tr(td(s.pattern), td(s.desc))
}
