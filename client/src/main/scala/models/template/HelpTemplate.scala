package models.template

import utils.{KeyboardShortcut, NumberUtils}

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
              div(cls := "left")(a(cls := "previous-tip-link theme-text", href := "")("Previous")),
              div(cls := "right")(a(cls := "next-tip-link theme-text", href := "")("Next")),
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
      ),
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "z-depth-1 help-panel")(
            h5("Connection"),
            div(
              "Connected, with a latency of [",
              span(cls := "help-latency-label")(NumberUtils.withCommas(0)),
              "ms]. We've sent ",
              span(cls := "help-sent-label")(NumberUtils.withCommas(0)),
              " and received ",
              span(cls := "help-received-label")(NumberUtils.withCommas(0)),
              " messages."
            )
          )
        )
      )
    )
    StaticPanelTemplate.cardRow(content, iconAndTitle = Some(Icons.help -> span("Database Flow Help")))
  }

  private[this] def patternToRow(s: KeyboardShortcut) = tr(td(s.pattern), td(s.desc))
}
