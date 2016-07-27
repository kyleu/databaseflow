package ui

import java.util.UUID

import org.scalajs.jquery.{jQuery => $}
import ui.query.SqlManager

import scala.io.Source

object EditorManager {
  def onSave(id: UUID) = {
    val queryPanel = $(s"#panel-$id")
    val saveLink = $(".save-query-link", queryPanel)
    if (saveLink.length != 1) {
      utils.Logging.warn(s"Found [${queryPanel.length}] panels and [${saveLink.length}] links for query [$id].")
    }
    saveLink.click()
  }

  def onRun(id: UUID) = {
    val queryPanel = $(s"#panel-$id")
    val runLink = $(".run-query-link", queryPanel)
    if (runLink.length != 1) {
      utils.Logging.warn(s"Found [${queryPanel.length}] panels and [${runLink.length}] links for query [$id].")
    }
    runLink.click()
  }

  def highlightError(queryId: UUID, sql: String, error: String, line: Option[Int], position: Option[Int]) = SqlManager.getEditor(queryId).foreach { editor =>
    //utils.Logging.debug(s"Query error [$error] at [${line.getOrElse("?")}:${position.getOrElse("?")}] for query [$queryId].")
    val fullSql = SqlManager.getSql(queryId)
    fullSql.indexOf(sql) match {
      case -1 => // no op
      case idx =>
        val lines = Source.fromString(sql).getLines().toSeq.zipWithIndex

    }
  }

  def clearError(queryId: UUID, sql: String) = SqlManager.getEditor(queryId).foreach { editor =>
    //utils.Logging.debug(s"Query [$queryId] was checked successfully.")
  }
}
