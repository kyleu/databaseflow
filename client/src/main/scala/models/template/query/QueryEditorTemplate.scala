package models.template.query

import java.util.UUID

import models.engine.DatabaseEngine
import models.query.SavedQuery
import models.template.{Icons, StaticPanelTemplate}
import utils.Messages

import scalatags.Text.TypedTag
import scalatags.Text.all._

object QueryEditorTemplate {
  private[this] def linksFor(engine: DatabaseEngine) = Seq(
    Some(a(cls := "run-query-link theme-text", href := "#")(Messages("query.run"))),
    Some(a(cls := "run-query-all-link theme-text initially-hidden", href := "#")(Messages("query.run.all"))),
    Some(a(cls := "run-query-selection-link theme-text initially-hidden", href := "#")(Messages("query.run.selection"))),
    if (engine.cap.explain.isDefined) { Some(a(cls := "explain-query-link theme-text", href := "#")(Messages("query.explain"))) } else { None },
    if (engine.cap.analyze.isDefined) { Some(a(cls := "analyze-query-link theme-text", href := "#")(Messages("query.analyze"))) } else { None }
  ).flatten

  def forAdHocQuery(engine: DatabaseEngine, queryId: UUID, queryName: String, sql: String) = {
    val links = linksFor(engine) :+ a(cls := "save-query-link right theme-text first-right-link", href := "#")(Messages("th.save"))
    queryPanel(queryId, queryName, sql, Icons.adHocQuery, links)
  }

  val savedQueryEditLinks = Seq(
    a(cls := "save-query-link right theme-text first-right-link", href := "#")(Messages("th.save")),
    a(cls := "settings-query-link right theme-text", href := "#")(Messages("th.settings")),
    a(cls := "save-as-query-link right theme-text", href := "#")(Messages("th.save.new")),
    a(cls := "delete-query-link right theme-text", href := "#")(Messages("th.delete"))
  )

  val savedQueryViewLinks = Seq(a(cls := "save-as-query-link right theme-text first-right-link", href := "#")(Messages("th.save.new")))

  def forSavedQuery(engine: DatabaseEngine, sq: SavedQuery, userId: UUID) = {
    val canEdit = userId == sq.owner
    val modificationLinks = if (canEdit) {
      savedQueryEditLinks
    } else {
      savedQueryViewLinks
    }
    val links = linksFor(engine) ++ modificationLinks
    queryPanel(sq.id, sq.name, sq.sql, Icons.savedQuery, links)
  }

  private[this] def queryPanel(queryId: UUID, queryName: String, sql: String, icon: String, links: Seq[TypedTag[String]]) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.panelRow(
        content = div(
          div(id := s"sql-textarea-$queryId", cls := "sql-textarea")(sql),
          div(cls := "sql-parameters initially-hidden")
        ),
        iconAndTitle = Some(
          icon -> span(span(cls := "query-title")(queryName), span(cls := "unsaved-status", title := Messages("query.unsaved.changes"))("*"))
        ),
        actions = links
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
