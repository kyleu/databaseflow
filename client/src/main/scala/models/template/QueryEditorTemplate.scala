package models.template

import java.util.UUID

import models.engine.DatabaseEngine
import models.query.SavedQuery

import scalatags.Text.TypedTag
import scalatags.Text.all._

object QueryEditorTemplate {
  private[this] def linksFor(engine: DatabaseEngine) = Seq(
    Some(a(cls := "run-query-link theme-text", href := "#")("Run")),
    Some(a(cls := "run-query-link theme-text", href := "#")("Export")),
    if (engine.explain.isDefined) { Some(a(cls := "explain-query-link theme-text", href := "#")("Explain")) } else { None },
    if (engine.analyze.isDefined) { Some(a(cls := "analyze-query-link theme-text", href := "#")("Analyze")) } else { None }
  ).flatten

  def forAdHocQuery(engine: DatabaseEngine, queryId: UUID, queryName: String, sql: String) = {
    val links = linksFor(engine) :+ a(cls := "save-query-link right theme-text", href := "#")("Save")
    queryPanel(queryId, queryName, None, sql, Icons.adHocQuery, links)
  }

  def forSavedQuery(engine: DatabaseEngine, sq: SavedQuery, userId: Option[UUID]) = {
    val modificationLinks = userId match {
      case Some(uid) => if (sq.owner.contains(uid)) {
        Seq(
          a(cls := "save-query-link right theme-text", href := "#")("Save"),
          a(cls := "settings-query-link right theme-text", href := "#")("Settings"),
          a(cls := "save-as-query-link right theme-text", href := "#")("Save As New"),
          a(cls := "delete-query-link right theme-text", href := "#")("Delete")
        )
      } else {
        Seq(
          a(cls := "save-as-query-link right theme-text", href := "#")("Save As New")
        )
      }
      case None => Seq.empty
    }
    val links = linksFor(engine) ++ modificationLinks
    queryPanel(sq.id, sq.name, sq.description, sq.sql, Icons.savedQuery, links)
  }

  private[this] def queryPanel(queryId: UUID, queryName: String, description: Option[String], sql: String, icon: String, links: Seq[TypedTag[String]]) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(
        div(id := s"sql-textarea-$queryId", cls := "sql-textarea", style := "width: 100%;")(sql),
        Some(icon -> queryName),
        actions = links
      ),
      div(id := s"workspace-$queryId")
    )
  }
}
