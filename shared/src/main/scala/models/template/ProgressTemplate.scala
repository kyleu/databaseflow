package models.template

import java.util.UUID

import scalatags.Text.all._

object ProgressTemplate {
  def loadingPanel(queryId: UUID, title: String, resultId: UUID) = {
    div(id := resultId.toString, cls := "panel")(
      StaticPanelTemplate.cardRow(
        title = title,
        content = div("Loading..."),
        icon = Some(Icons.loading + " " + Icons.spin),
        showClose = false
      )
    )
  }
}
