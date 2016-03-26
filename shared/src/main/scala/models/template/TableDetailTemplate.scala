package models.template

import java.util.UUID

import models.schema.Table

import scalatags.Text.all._

object TableDetailTemplate {
  def forTable(queryId: UUID, t: Table) = {
    val description = t.description.getOrElse("")
    val links = Seq(
      Some(a(cls := "view-data-link", href := "#")("View Data")),
      t.indexes.headOption.map(i => a(cls := "right indexes-link", href := "#")("Indexes")),
      t.foreignKeys.headOption.map(fk => a(cls := "right foreign-keys-link", href := "#")("Foreign Keys")),
      t.columns.headOption.map(i => a(cls := "right columns-link", href := "#")("Columns"))
    ).flatten

    div(id := s"panel-$queryId", cls := "workspace-panel")(
      div(cls := "row")(
        div(cls := "col s12")(
          div(cls := "card")(
            div(cls := "card-content")(
              span(cls := "card-title")(
                i(cls := "title-icon fa fa-folder-open-o"),
                t.name,
                i(cls := "right fa fa-close")
              ),
              div(description)
            ),
            div(cls := "card-action")(links: _*)
          )
        )
      ),
      div(id := s"workspace-$queryId")
    )
  }

  def foreignKeysForTable(resultId: UUID, queryId: UUID, t: Table) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := "title-icon fa fa-sign-out"),
              "Foreign Keys for ",
              em(t.name),
              i(cls := "right fa fa-close")
            ),
            if (t.foreignKeys.isEmpty) {
              div("No foreign keys are available for this table.")
            } else {
              table(
                thead(
                  tr(
                    td("Name"),
                    td("Source Columns"),
                    td("Target Table"),
                    td("Target Columns")
                  )
                ),
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
          )
        )
      )
    )
  }

  def indexesForTable(resultId: UUID, queryId: UUID, t: Table) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := "title-icon fa fa-sign-in"),
              "Indexes for ",
              em(t.name),
              i(cls := "right fa fa-close")
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

  def columnsForTable(resultId: UUID, queryId: UUID, t: Table) = {
    div(id := resultId.toString, cls := "row")(
      div(cls := "col s12")(
        div(cls := "card")(
          div(cls := "card-content")(
            span(cls := "card-title")(
              i(cls := "title-icon fa fa-sign-in"),
              "Columns for ",
              em(t.name),
              i(cls := "right fa fa-close")
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
