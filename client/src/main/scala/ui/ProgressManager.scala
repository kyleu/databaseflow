package ui

import java.util.UUID

import models.CancelQuery
import models.template.ProgressTemplate
import org.scalajs.jquery.{jQuery => $}
import util.{TemplateUtils, NetworkMessage}

import scala.scalajs.js.timers
import scalatags.Text.TypedTag

object ProgressManager {
  var activeQueries = Map.empty[UUID, UUID]

  lazy val workspace = $("#workspace")

  def startProgress(queryId: UUID, resultId: UUID, title: String): Unit = {
    activeQueries.get(queryId) match {
      case Some(active) if active == resultId => throw new IllegalStateException(s"Already started progress for query [$queryId] with result [$resultId].")
      case Some(active) => throw new IllegalStateException(s"Cannot start progress for query [$queryId] with [$resultId], already processing [$active].")
      case None => // No op
    }

    val html = ProgressTemplate.loadingPanel(queryId, title, resultId).render

    val queryWorkspace = $(s"#workspace-$queryId", workspace)
    if (queryWorkspace.length != 1) {
      throw new IllegalStateException(s"No query workspace available for result [$resultId] for query [$queryId].")
    }
    val existingResult = $(s"#$resultId", queryWorkspace)
    if (existingResult.length == 0) {
      queryWorkspace.html(html)
    } else {
      existingResult.html(html)
    }

    val cancelLink = $(".cancel-query-link", queryWorkspace)
    TemplateUtils.clickHandler(cancelLink, _ => {
      NetworkMessage.sendMessage(CancelQuery(queryId, resultId))
    })

    val timer = $(".timer", queryWorkspace)
    if (timer.length != 1) {
      throw new IllegalStateException(s"Found [${timer.length}] timers for result [$resultId].")
    }

    def incrementTimer(): Unit = {
      val newVal = timer.text().toInt + 1
      timer.text(newVal.toString)
      if (activeQueries.get(queryId).contains(resultId)) {
        timers.setTimeout(1000)(incrementTimer())
      }
    }

    timers.setTimeout(1000)(incrementTimer())

    activeQueries = activeQueries + (queryId -> resultId)
  }

  def completeProgress(queryId: UUID, resultId: UUID, index: Int, content: TypedTag[String]): Unit = {
    val queryWorkspace = $(s"#workspace-$queryId", workspace)
    if (queryWorkspace.length != 1) {
      throw new IllegalStateException(s"No query workspace available for result [$resultId] for query [$queryId].")
    }
    val existingResult = $(s"#$resultId", queryWorkspace)
    if (existingResult.length == 0) {
      if (index == 0) {
        queryWorkspace.html(content.render)
      } else {
        queryWorkspace.append(content.render)
      }
    } else {
      existingResult.replaceWith(content.render)
    }

    TemplateUtils.relativeTime()

    activeQueries = activeQueries - queryId
  }
}
