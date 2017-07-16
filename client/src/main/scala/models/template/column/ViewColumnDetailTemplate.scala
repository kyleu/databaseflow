package models.template.column

import models.schema.Column
import util.Messages

import scalatags.Text.all._

object ViewColumnDetailTemplate {
  def columnPanel(columns: Seq[Column]) = {
    tableFor(columns)
  }

  private[this] def tableFor(columns: Seq[Column]) = table(cls := "bordered highlight responsive-table")(
    thead(tr(
      th(Messages("th.name")),
      th(Messages("th.type"))
    )),
    tbody(columns.map { col =>
      tr(
        td(ColumnTemplate.linkFor(col)),
        td(col.columnType.toString)
      )
    })
  )
}
