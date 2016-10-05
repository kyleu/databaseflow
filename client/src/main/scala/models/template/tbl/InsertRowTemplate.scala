package models.template.tbl

import models.schema.Column
import utils.Messages

import scalatags.Text.all._

object InsertRowTemplate {
  private[this] def getLabel(col: Column) = Seq(
    if (col.notNull && col.defaultValue.isEmpty) {
      input(`type` := "checkbox", id := s"insert-row-toggle-${col.name}", checked)
    } else {
      input(`type` := "checkbox", id := s"insert-row-toggle-${col.name}")
    },
    label(`for` := s"insert-row-toggle-${col.name}")("")
  )

  private[this] def getInput(v: String, col: Column) = input(
    placeholder := (if (col.notNull && col.defaultValue.isEmpty) {
      col.columnType + ", required"
    } else {
      col.columnType.toString
    }),
    id := s"insert-row-input-${col.name}",
    cls := "insert-row-input",
    `type` := "text",
    data("col") := col.name,
    value := v
  )

  private[this] def tbodyForColumns(cols: Seq[Column]) = tbody(cols.flatMap { col =>
    Seq(
      tr(
        td(colspan := 2)(col.name)
      ),
      tr(
        td(cls := "use-toggle")(getLabel(col)),
        td(cls := "input-field")(
          getInput("", col),
          div(cls := "insert-row-error", id := s"insert-row-error-${col.name}")
        )
      )
    )
  }: _*)

  def forColumns(cols: Seq[Column]) = div(
    h5(Messages("query.insert")),
    div(cls := "insert-row-error", id := "insert-row-error-general"),
    div(cls := "insert-row-container row")(table(tbodyForColumns(cols)))
  )
}
