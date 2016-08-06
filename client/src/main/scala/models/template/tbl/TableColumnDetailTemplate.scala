package models.template.tbl

import models.schema.Column
import utils.Messages

import scalatags.Text.all._

object TableColumnDetailTemplate {
  def columnPanel(columns: Seq[Column]) = {
    tableFor(columns)
  }

  private[this] def tableFor(columns: Seq[Column]) = table(cls := "bordered highlight responsive-table")(
    thead(
      tr(
        th(Messages("th.name")),
        th(title := Messages("th.primary.key"))("PK"),
        th(title := Messages("th.not.null"))("NN"),
        th(Messages("th.type")),
        th(Messages("th.default"))
      )
    ),
    tbody(
      columns.flatMap { col =>
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
