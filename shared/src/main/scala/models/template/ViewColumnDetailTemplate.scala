package models.template

import java.util.UUID

import models.schema.View

import scalatags.Text.all._

object ViewColumnDetailTemplate {
  def columnsForView(resultId: UUID, queryId: UUID, v: View) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := s"title-icon fa ${Icons.columns}"),
              "Columns for ",
              em(v.name),
              i(cls := s"right fa ${Icons.close}")
            ),
            if (v.columns.isEmpty) {
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
          )
        )
      )
    )
  }
}
