package ui

import java.util.UUID

import models.template.{FeedbackTemplate, Icons}
import org.scalajs.jquery.{JQueryAjaxSettings, JQueryXHR, jQuery => $}
import services.NotificationService
import ui.query.QueryManager
import ui.tabs.TabManager
import utils.{Logging, TemplateUtils}

import scala.scalajs.js
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

      def close() = if (QueryManager.closeQuery(feedbackId)) {
        isOpen = false
      }

      TabManager.addTab(feedbackId, "feedback", "Feedback", Icons.feedback, close)
      QueryManager.activeQueries = QueryManager.activeQueries :+ feedbackId

      val queryPanel = $(s"#panel-$feedbackId")

      TemplateUtils.clickHandler($(".submit-feedback", queryPanel), jq => {
        val email = $("#feedback-email-input", queryPanel).value().toString
        val content = $("#feedback-content-input", queryPanel).value().toString
        submitFeedback(email, content)
      })

      isOpen = true
    }
  }

  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def submitFeedback(email: String, content: String) = {
    val url = "https://databaseflow.com/feedback?ajax=true"
    $.ajax(js.Dynamic.literal(
      url = url,
      data = js.Dynamic.literal(
        id = UUID.randomUUID.toString,
        email = email,
        content = content
      ),
      success = { (data: js.Any, textStatus: String, jqXHR: JQueryXHR) =>
        NotificationService.info("Feedback Success", "Thanks!")
        if (QueryManager.closeQuery(feedbackId)) {
          isOpen = false
        }
      },
      error = { (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) =>
        Logging.info(s"Ajax Error: jqXHR=$jqXHR, text=$textStatus, err=$errorThrow")
      },
      `type` = "POST"
    ).asInstanceOf[JQueryAjaxSettings])
  }
}
