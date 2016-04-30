package models.template

import java.util.UUID

import scalatags.Text.all._

object ProcedureDetailTemplate {
  def forProcedure(queryId: UUID, procedureName: String) = {
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(div(), Some(Icons.procedure -> procedureName)),
      div(id := s"workspace-$queryId")
    )
  }
}
