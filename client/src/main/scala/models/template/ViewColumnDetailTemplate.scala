package models.template

import models.schema.Column

import scalatags.Text.all._

object ViewColumnDetailTemplate {
  def columnPanel(columns: Seq[Column]) = {
    tableFor(columns)
  }

  private[this] def tableFor(columns: Seq[Column]) = table(cls := "bordered highlight responsive-table")(
    thead(
      tr(
        th("Name"),
        th("Type")
      )
    ),
    tbody(
      columns.map { col =>
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
