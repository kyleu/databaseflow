package models.template

import java.util.UUID

import models.schema.View

import scalatags.Text.all._

object ViewColumnDetailTemplate {
  def columnsForView(resultId: UUID, queryId: UUID, v: View) = {
    val content = if (v.columns.isEmpty) {
      div("No columns are available for this view.")
    } else {
      table(cls := "bordered highlight responsive-table")(
        thead(
          tr(
            th("Name"),
            th("Type")
          )
        ),
        tbody(
          v.columns.map { col =>
            val nn = col.notNull.toString
            val defaultVal = col.defaultValue.getOrElse("")
            tr(
              td(col.name),
              td(col.columnType.toString)
            )
          }
        )
      )
    }

    div(id := resultId.toString)(
      StaticPanelTemplate.cardRow("Columns for " + v.name, content, icon = Some(Icons.columns))
    )
  }
}
