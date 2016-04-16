package models.template

import java.util.UUID

import models.engine.DatabaseEngine
import models.query.SavedQuery

import scalatags.Text.TypedTag
import scalatags.Text.all._

object QueryEditorTemplate {
  private[this] def linksFor(engine: DatabaseEngine) = Seq(
    Some(a(cls := "run-query-link", href := "#")("Run")),
    if (engine.explain.isDefined) { Some(a(cls := "explain-query-link", href := "#")("Explain")) } else { None },
    if (engine.analyze.isDefined) { Some(a(cls := "analyze-query-link", href := "#")("Analyze")) } else { None }
  ).flatten

  def forAdHocQuery(engine: DatabaseEngine, queryId: UUID, queryName: String, sql: String) = {
    val links = linksFor(engine) :+ a(cls := "save-query-link right", href := "#")("Save")
    queryPanel(queryId, queryName, None, sql, Icons.adHocQuery, links)
  }

  def forSavedQuery(engine: DatabaseEngine, sq: SavedQuery, userId: UUID) = {
    val modificationLinks = if (sq.owner.contains(userId)) {
      Seq(
        a(cls := "save-query-link right", href := "#")("Save"),
        a(cls := "settings-query-link right", href := "#")("Settings"),
        a(cls := "save-as-query-link right", href := "#")("Save As New"),
        a(cls := "delete-query-link right", href := "#")("Delete")
      )
    } else {
      Seq(
        a(cls := "save-as-query-link right", href := "#")("Save As New")
      )
    }
    val links = linksFor(engine) ++ modificationLinks
    queryPanel(sq.id, sq.name, sq.description, sq.sql, Icons.savedQuery, links)
  }

  def forView(engine: DatabaseEngine, queryId: UUID, viewName: String, description: Option[String], sql: String) = {
    val modificationLinks = Seq(
      a(cls := "right definition-link initially-hidden", href := "#")("Definition"),
      a(cls := "right columns-link initially-hidden", href := "#")("Columns")
    )
    queryPanel(queryId, viewName, description, sql, Icons.view, linksFor(engine) ++ modificationLinks)
  }

  private[this] def queryPanel(queryId: UUID, queryName: String, description: Option[String], sql: String, icon: String, links: Seq[TypedTag[String]]) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(
        queryName,
        div(id := s"sql-textarea-$queryId", cls := "sql-textarea", style := "width: 100%;")(sql),
        Some(icon),
        actions = Some(links)
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
