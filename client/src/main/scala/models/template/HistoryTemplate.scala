package models.template

import scalatags.Text.all._

object HistoryTemplate {
  def content() = {
    val content = div(id := "history-panel")(
      div("Loading query history...")
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.history -> "Query History"),
      actions = Seq(
        div(a(cls := "refresh-history-link", href := "")("Refresh"))
      )
    )
  }
}
