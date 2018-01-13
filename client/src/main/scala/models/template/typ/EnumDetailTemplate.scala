package models.template.typ

import java.util.UUID

import models.schema.EnumType
import models.template.{Icons, StaticPanelTemplate}

import scalatags.Text.all._

object EnumDetailTemplate {
  def forEnum(queryId: UUID, enum: EnumType) = {
    val content = div(ul(cls := "collection")(enum.values.map(v => li(cls := "collection-item")(v)): _*))

    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.row(StaticPanelTemplate.panel(content, iconAndTitle = Some(Icons.enum -> span(enum.key)))),
      div(id := s"workspace-$queryId")
    )
  }
}
