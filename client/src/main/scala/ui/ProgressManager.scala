package ui

import java.util.UUID

import models.template.{ Icons, ProgressTemplate }
import org.scalajs.jquery.{ jQuery => $ }

import scalatags.Text.TypedTag

object ProgressManager {
  var activeQueries = Map.empty[UUID, (UUID, () => Unit)]

  lazy val workspace = $("#workspace")

  def startProgress(queryId: UUID, resultId: UUID, onComplete: () => Unit, icon: String, title: String): Unit = {
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

    activeQueries = activeQueries + (queryId -> (resultId -> onComplete))
  }

  def completeProgress(queryId: UUID, resultId: UUID, icon: String, title: String, content: TypedTag[String], actions: Seq[TypedTag[String]]): Unit = {
    val onComplete = activeQueries.get(queryId) match {
      case Some(rid) if rid._1 == resultId => rid._2
      case Some(rid) => throw new IllegalStateException(s"Active progress for query [$queryId] is [${rid._1}], not expected [$resultId].")
      case None => throw new IllegalStateException(s"No active progress for query [$queryId].")
    }

    val panel = $(s"#$resultId", workspace)
    if (panel.length != 1) {
      throw new IllegalStateException(s"Found [${panel.length}] panels for result [$resultId].")
    }

    val iconEl = $(".title-icon", panel)
    if (iconEl.length != 1) {
      throw new IllegalStateException(s"Found [${iconEl.length}] icon elements for result [$resultId].")
    }
    iconEl.removeClass(Icons.loading).removeClass(Icons.spin).addClass(icon)

    val titleEl = $(".title", panel)
    if (titleEl.length != 1) {
      throw new IllegalStateException(s"Found [${titleEl.length}] status elements for result [$resultId].")
    }
    titleEl.text(title)

    val contentEl = $(".content", panel)
    if (contentEl.length != 1) {
      throw new IllegalStateException(s"Found [${contentEl.length}] content elements for result [$resultId].")
    }
    contentEl.html(content.render)

    if (actions.nonEmpty) {
      val actionsEl = $(".card-action", panel)
      if (actionsEl.length != 1) {
        throw new IllegalStateException(s"Found [${actionsEl.length}] actions elements for result [$resultId].")
      }
      actionsEl.html(actions.map(_.render).mkString("\n"))
      actionsEl.show()
    }

    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()

    activeQueries = activeQueries - queryId

    onComplete()
  }
}
