package models.template.results

import java.util.UUID

import models.query.SharedResult
import models.template.{Icons, StaticPanelTemplate}

import scalatags.Text.TypedTag
import scalatags.Text.all._

object SharedResultTemplate {
  def forSharedResult(sr: SharedResult) = {
    queryPanel(sr.id, sr.title, Nil)
  }

  private[this] def queryPanel(resultId: UUID, title: String, links: Seq[TypedTag[String]]) = {
    div(id := s"panel-$resultId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(
        content = div("Hello!"),
        iconAndTitle = Some(Icons.sharedResult -> span(title)),
        actions = links
      ),
      div(id := s"workspace-$resultId")
    )
  }
}
