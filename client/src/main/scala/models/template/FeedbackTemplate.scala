package models.template

import scalatags.Text.all._

object FeedbackTemplate {
  def content() = {
    val content = div(id := "feedback-panel")(
      "Feedback!"
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.feedback -> "Feedback")
    )
  }
}
