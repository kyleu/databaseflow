package ui

import java.util.UUID

import models.template.ProgressTemplate
import org.scalajs.jquery.{ jQuery => $ }
import utils.JQueryUtils

import scala.scalajs.js.timers
import scalatags.Text.TypedTag

object ProgressManager {
  var activeQueries = Map.empty[UUID, (UUID, () => Unit)]

  lazy val workspace = $("#workspace")

  def startProgress(queryId: UUID, resultId: UUID, onComplete: () => Unit, title: String): Unit = {
    activeQueries.get(queryId) match {
      case Some(active) if active._1 == resultId => throw new IllegalStateException(s"Already started progress for query [$queryId] with result [$resultId].")
      case Some(active) => throw new IllegalStateException(s"Cannot start progress for query [$queryId] with [$resultId], already processing [${active._1}].")
      case None => // No op
    }

    val html = ProgressTemplate.loadingPanel(queryId, title, resultId).render

    val queryWorkspace = $(s"#workspace-$queryId", workspace)
    if (queryWorkspace.length != 1) {
      throw new IllegalStateException(s"No query workspace available for result [$resultId] for query [$queryId].")
    }
    queryWorkspace.html(html)

    val timer = $(".timer", queryWorkspace)
    if (timer.length != 1) {
      throw new IllegalStateException(s"Found [${timer.length}] timers for result [$resultId].")
    }

    def incrementTimer(): Unit = {
      val newVal = timer.text().toInt + 1
      timer.text(newVal.toString)
      if (activeQueries.get(queryId).exists(_._1 == resultId)) {
        timers.setTimeout(1000)(incrementTimer())
      }
    }

    timers.setTimeout(1000)(incrementTimer())

    activeQueries = activeQueries + (queryId -> (resultId -> onComplete))
  }

  def completeProgress(queryId: UUID, resultId: UUID, content: TypedTag[String]): Unit = {
    val onComplete = activeQueries.get(queryId) match {
      case Some(rid) if rid._1 == resultId => rid._2
      case Some(rid) => throw new IllegalStateException(s"Active progress for query [$queryId] is [${rid._1}], not expected [$resultId].")
      case None => throw new IllegalStateException(s"No active progress for query [$queryId].")
    }

    val queryWorkspace = $(s"#workspace-$queryId", workspace)
    if (queryWorkspace.length != 1) {
      throw new IllegalStateException(s"No query workspace available for result [$resultId] for query [$queryId].")
    }
    queryWorkspace.html(content.render)

    JQueryUtils.relativeTime()

    activeQueries = activeQueries - queryId

    onComplete()
  }
}
