package models.template.column

import models.schema.Column
import utils.Messages

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
      val nn = col.notNull.toString
      val defaultVal = col.defaultValue.getOrElse("")
      tr(
        td(ColumnTemplate.linkFor(col)),
        td(col.columnType.toString)
      )
    })
  )
}
