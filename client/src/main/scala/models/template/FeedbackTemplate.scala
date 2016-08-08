package models.template

import utils.Messages

import scalatags.Text.all._

object FeedbackTemplate {
  def content(email: String) = {
    val content = div(id := "feedback-panel")(
      div(cls := "row")(
        form(cls := "col s12")(
          div(cls := "row")(
            div(cls := "col s12")(p(Messages("feedback.notice", utils.Config.projectName))),
            div(cls := "input-field col s12")(
              input(id := "feedback-email-input", cls := "validate", `type` := "email", value := email, placeholder := Messages("feedback.email"))()
            ),
            div(cls := "input-field col s12")(
              textarea(id := "feedback-content-input", cls := "materialize-textarea", placeholder := Messages("feedback.prompt"))()
            )
          )
        )
      )
    )

    StaticPanelTemplate.cardRow(
      content = content,
      iconAndTitle = Some(Icons.feedback -> span("Feedback")),
      actions = Seq(
        a(href := "", cls := "theme-text right submit-feedback")(Messages("feedback.submit")),
        div(style := "clear: both;")
      )
    )
  }
}
