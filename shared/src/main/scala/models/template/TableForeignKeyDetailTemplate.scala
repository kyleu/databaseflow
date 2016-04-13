package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableForeignKeyDetailTemplate {
  def foreignKeysForTable(resultId: UUID, queryId: UUID, t: Table) = {
    val content = if (t.foreignKeys.isEmpty) {
      div("No foreign keys are available for this table.")
    } else {
      table(
        thead(tr(
          td("Name"),
          td("Source Columns"),
          td("Target Table"),
          td("Target Columns")
        )),
        tbody(
          t.foreignKeys.map { key =>
            tr(
              td(key.name),
              td(key.references.map(_.source).mkString(", ")),
              td(key.targetTable),
              td(key.references.map(_.target).mkString(", "))
            )
          }
        )
      )
    }

    div(id := resultId.toString)(
      StaticPanelTemplate.cardRow("Foreign Keys for " + t.name, content, icon = Some(Icons.foreignKeys))
    )
  }
}
