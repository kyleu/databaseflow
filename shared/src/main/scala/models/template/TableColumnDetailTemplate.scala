package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableColumnDetailTemplate {
  def columnsForTable(resultId: UUID, queryId: UUID, t: Table) = {
    val content = if (t.columns.isEmpty) {
      div("No columns are available for this table.")
    } else {
      table(cls := "bordered highlight responsive-table")(
        thead(
          tr(
            th("Name"),
            th(title := "Primary Key")("PK"),
            th(title := "Not Null")("NN"),
            th("Type"),
            th("Default")
          )
        ),
        tbody(
          t.columns.flatMap { col =>
            val pk = col.primaryKey.toString
            val nn = col.notNull.toString
            val defaultVal = col.defaultValue.getOrElse("")
            Seq(tr(
              td(col.name),
              td(pk),
              td(nn),
              td(col.columnType.toString),
              td(defaultVal)
            )) ++ col.description.map(d => tr(colspan := 5)(em(d))).toSeq
          }
        )
      )
    }

    div(id := resultId.toString)(
      StaticPanelTemplate.cardRow(
        "Columns for " + t.name,
        content = content,
        icon = Some(Icons.columns)
      )
    )
  }
}
