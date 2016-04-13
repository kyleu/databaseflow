package models.template

import java.util.UUID

import scalatags.Text.all._

object TableDetailTemplate {
  def forTable(queryId: UUID, tableName: String) = {
    val content = div(
      div(cls := "description")(""),
      div(cls := "summary")("")
    )
    val actions = Seq(
      a(cls := "view-data-link", href := "#")("View Data"),
      a(cls := "right definition-link initially-hidden", href := "#")("Definition"),
      a(cls := "right foreign-keys-link initially-hidden", href := "#")("Foreign Keys"),
      a(cls := "right indexes-link initially-hidden", href := "#")("Indexes"),
      a(cls := "right columns-link initially-hidden", href := "#")("Columns")
    )
    div(id := s"panel-$queryId", cls := "workspace-panel")(
      StaticPanelTemplate.cardRow(tableName, content, icon = Some(Icons.table), actions = Some(actions)),
      div(id := s"workspace-$queryId")
    )
  }
}
