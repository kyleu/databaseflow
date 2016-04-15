package models.template

import java.util.UUID

import models.schema.View

import scalatags.Text.all._

object ViewDefinitionTemplate {
  def definitionPanel(definition: String) = {
    pre(cls := "pre-wrap")(definition)
  }

  def definitionForView(resultId: UUID, queryId: UUID, v: View) = {
    val content = v.definition match {
      case Some(definition) => pre(cls := "pre-wrap")(definition)
      case None => div("No definition is available for this view.")
    }

    div(id := resultId.toString)(
      StaticPanelTemplate.cardRow("View Definition for " + v.name, content, icon = Some(Icons.definition))
    )
  }
}
