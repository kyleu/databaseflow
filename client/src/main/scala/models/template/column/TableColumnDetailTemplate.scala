package models.template.column

import models.schema.Column
import utils.Messages

import scalatags.Text.all._

object TableColumnDetailTemplate {
  def columnPanel(columns: Seq[Column]) = {
    tableFor(columns)
  }

  def getRows(col: Column) = {
    val pk = col.primaryKey.toString
    val nn = col.notNull.toString
    val defaultVal = col.defaultValue.getOrElse("")
    Seq(tr(
      td(ColumnTemplate.linkFor(col)),
      td(pk),
      td(nn),
      td(col.columnType.toString),
      td(defaultVal)
    )) ++ col.description.flatMap {
      case d if d.trim.nonEmpty => Some(tr(td(colspan := 5)(d)))
      case _ => None
    }.toSeq
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
    tbody(columns.flatMap { col => getRows(col) })
  )
}
