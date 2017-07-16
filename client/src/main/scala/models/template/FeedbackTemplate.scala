package models.template

import util.Messages

import scalatags.Text.all._

object FeedbackTemplate {
  def content(email: String) = {
    val content = div(id := "feedback-panel")(
      p(Messages("feedback.notice", util.Config.projectName)),
      div(cls := "input-field")(
        input(id := "feedback-email-input", cls := "validate", `type` := "email", value := email, placeholder := Messages("feedback.email"))()
      ),
      div(cls := "input-field")(
        textarea(id := "feedback-content-input", cls := "materialize-textarea", placeholder := Messages("feedback.prompt"))()
      )
    )

    StaticPanelTemplate.row(StaticPanelTemplate.panel(
      content = content,
      iconAndTitle = Some(Icons.feedback -> span(Messages("feedback.title"))),
      actions = Seq(
        a(href := "", cls := "theme-text right submit-feedback")(Messages("feedback.submit")),
        div(style := "clear: both;")
      )
    ))
  }
}
