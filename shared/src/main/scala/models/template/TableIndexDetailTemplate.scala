package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableIndexDetailTemplate {
  def indexesForTable(resultId: UUID, queryId: UUID, t: Table) = {
    val content = if (t.indexes.isEmpty) {
      div("No indexes are available for this table.")
    } else {
      table(cls := "bordered highlight responsive-table")(
        thead(
          tr(
            td("Name"),
            td("Unique"),
            td("Type"),
            td("Columns")
          )
        ),
        tbody(
          t.indexes.map { idx =>
            val uniq = idx.unique.toString
            tr(
              td(idx.name),
              td(uniq),
              td(idx.indexType),
              td(idx.columns.mkString(", "))
            )
          }
        )
      )
    }

    div(id := resultId.toString)(
      StaticPanelTemplate.cardRow("Indexes for " + t.name, content, icon = Some(Icons.indexes))
    )
  }
}
