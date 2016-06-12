package ui

import java.util.UUID

import models.template.{FeedbackTemplate, Icons}
import org.scalajs.jquery.{JQueryAjaxSettings, JQueryXHR, jQuery => $}
import services.NotificationService
import ui.query.QueryManager
import utils.Logging

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

      def close() = {
        isOpen = false
        QueryManager.closeQuery(feedbackId)
      }

      TabManager.addTab(feedbackId, "feedback", "Feedback", Icons.feedback, close)
      QueryManager.activeQueries = QueryManager.activeQueries :+ feedbackId

      val queryPanel = $(s"#panel-$feedbackId")

      utils.JQueryUtils.clickHandler($(".submit-feedback", queryPanel), (jq) => {
        val email = $("#feedback-email-input", queryPanel).value().toString
        val content = $("#feedback-content-input", queryPanel).value().toString
        submitFeedback(email, content)
      })

      isOpen = true
    }
  }

  @SuppressWarnings(Array("AsInstanceOf"))
  private[this] def submitFeedback(email: String, content: String) = {
    utils.Logging.info(s"Feedback from [$email]: $content")

    val url = "http://databaseflow.dev/feedback?ajax=true"
    $.ajax(js.Dynamic.literal(
      url = url,
      data = js.Dynamic.literal(
        id = UUID.randomUUID.toString,
        email = email,
        content = content
      ),
      success = { (data: js.Any, textStatus: String, jqXHR: JQueryXHR) =>
        NotificationService.info("Feedback Success", "Thanks!")
        isOpen = false
        QueryManager.closeQuery(feedbackId)
      },
      error = { (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) =>
        Logging.info(s"Error: jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
      },
      `type` = "POST"
    ).asInstanceOf[JQueryAjaxSettings])
  }
}
