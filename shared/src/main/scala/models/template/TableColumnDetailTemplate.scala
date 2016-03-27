package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableColumnDetailTemplate {
  def columnsForTable(resultId: UUID, queryId: UUID, t: Table) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := s"title-icon fa ${Icons.columns}"),
              "Columns for ",
              em(t.name),
              i(cls := s"right fa ${Icons.close}")
            ),
            if (t.columns.isEmpty) {
              div("No columns are available for this table.")
            } else {
              table(cls := "bordered highlight responsive-table")(
                thead(
                  tr(
                    td("Name"),
                    td("PK"),
                    td("NN"),
                    td("Type"),
                    td("Default")
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
          )
        )
      )
    )
  }
}
