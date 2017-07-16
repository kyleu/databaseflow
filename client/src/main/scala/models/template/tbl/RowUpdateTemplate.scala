package models.template.tbl

import models.schema.Column
import util.Messages

import scalatags.Text.all._

object RowUpdateTemplate {
  private[this] def getLabel(col: Column) = Seq(
    if (col.notNull && col.defaultValue.isEmpty) {
      input(`type` := "checkbox", id := s"row-update-toggle-${col.name}", checked)
    } else {
      input(`type` := "checkbox", id := s"row-update-toggle-${col.name}")
    },
    label(`for` := s"row-update-toggle-${col.name}")("")
  )

  private[this] def getInput(v: String, col: Column) = input(
    placeholder := (if (col.notNull && col.defaultValue.isEmpty) {
      col.columnType + ", required"
    } else {
      col.columnType.toString
    }),
    id := s"row-update-input-${col.name}",
    cls := "row-update-input",
    `type` := "text",
    data("col") := col.name,
    value := v
  )

  private[this] def tbodyForColumns(cols: Seq[Column], data: Map[String, String]) = tbody(cols.flatMap { col =>
    Seq(
      tr(
        td(colspan := 2)(col.name)
      ),
      tr(
        td(cls := "use-toggle")(getLabel(col)),
        td(cls := "input-field")(
          getInput(data.getOrElse(col.name, ""), col),
          div(cls := "row-update-error", id := s"row-update-error-${col.name}")
        )
      )
    )
  }: _*)

  def forColumns(insert: Boolean, name: String, cols: Seq[Column], data: Map[String, String]) = {
    val title = if (insert) { Messages("query.insert.title", name) } else { Messages("query.update.title", name) }
    div(
      h5(title),
      div(cls := "row-update-error", id := "row-update-error-general"),
      div(cls := "row-update-container row")(table(tbodyForColumns(cols, data)))
    )
  }
}
