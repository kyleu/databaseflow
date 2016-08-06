package models.template

import java.util.UUID

import utils.Messages

import scalatags.Text.all._

object ProcedureDetailTemplate {
  def forProcedure(queryId: UUID, procedureName: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(div(), Some(Icons.procedure -> span(procedureName))),
      div(id := s"workspace-$queryId")(Messages("general.coming.soon"))
    )
  }
}
