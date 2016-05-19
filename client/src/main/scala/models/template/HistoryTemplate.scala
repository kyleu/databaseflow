package models.template

import scalatags.Text.all._

object HistoryTemplate {
  def content() = {
    val content = div(id := "history-panel")(
      "History!"
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.history -> "Query History")
    )
  }
}
