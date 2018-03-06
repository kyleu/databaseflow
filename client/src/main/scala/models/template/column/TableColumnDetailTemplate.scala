package models.template.column

import models.schema.Column
import util.Messages

import scalatags.Text.all._

object TableColumnDetailTemplate {
  def columnPanel(columns: Seq[Column]) = {
    tableFor(columns)
  }

  def getRows(col: Column) = {
    val defaultVal = col.defaultValue.getOrElse("")
    Seq(tr(
      td(ColumnTemplate.linkFor(col)),
      td(col.primaryKey.toString),
      td(col.notNull.toString),
      td(title := col.sqlTypeName)(col.columnType.toString),
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
