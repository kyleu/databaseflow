package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableDefinitionTemplate {
  def definitionForTable(resultId: UUID, queryId: UUID, t: Table) = {
    val content = t.definition match {
      case Some(definition) => pre(cls := "pre-wrap")(definition)
      case None => div("No definition is available for this table.")
    }

    div(id := resultId.toString)(
      StaticPanelTemplate.cardRow(
        "Table Definition for " + t.name,
        content = content,
        icon = Some(Icons.definition)
      )
    )
  }
}
