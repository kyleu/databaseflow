package ui

import java.util.UUID

import models.template.{ FeedbackTemplate, Icons }
import org.scalajs.jquery.{ jQuery => $ }
import services.NotificationService

import scalatags.Text.all._

object FeedbackManager {
  private[this] val feedbackId = UUID.fromString("22222222-2222-2222-2222-222222222222")
  private[this] var isOpen = false

  def show() = {
    if (isOpen) {
      TabManager.selectTab(feedbackId)
    } else {
      val template = FeedbackTemplate.content(UserManager.email.getOrElse(""))
      val panelHtml = div(id := s"panel-$feedbackId", cls := "workspace-panel")(template)

      WorkspaceManager.append(panelHtml.toString)
      TabManager.addTab(feedbackId, "feedback", "Feedback", Icons.feedback)
      QueryManager.activeQueries = QueryManager.activeQueries :+ feedbackId

      val queryPanel = $(s"#panel-$feedbackId")

      utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
        isOpen = false
        QueryManager.closeQuery(feedbackId)
      })

      utils.JQueryUtils.clickHandler($(s".submit-feedback", queryPanel), (jq) => {
        val email = $("#feedback-email-input", queryPanel).value().toString
        val content = $("#feedback-content-input", queryPanel).value().toString
        submitFeedback(email, content)
      })

      isOpen = true
    }
  }

  private[this] def submitFeedback(email: String, content: String) = {
    utils.Logging.info(s"Feedback from [$email]: $content")
    NotificationService.info("Feedback Success", "Thanks!")
    isOpen = false
    QueryManager.closeQuery(feedbackId)
  }
}
