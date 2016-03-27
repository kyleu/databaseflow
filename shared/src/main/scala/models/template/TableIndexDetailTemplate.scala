package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableIndexDetailTemplate {
  def indexesForTable(resultId: UUID, queryId: UUID, t: Table) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := s"title-icon fa ${Icons.indexes}"),
              "Indexes for ",
              em(t.name),
              i(cls := s"right fa ${Icons.close}")
            ),
            if (t.indexes.isEmpty) {
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
          )
        )
      )
    )
  }
}
