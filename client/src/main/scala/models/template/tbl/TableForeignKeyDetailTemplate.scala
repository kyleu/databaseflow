package models.template.tbl

import models.schema.ForeignKey
import utils.Messages

import scalatags.Text.all._

object TableForeignKeyDetailTemplate {
  def foreignKeyPanel(foreignKeys: Seq[ForeignKey]) = {
    tableFor(foreignKeys)
  }

  private[this] def tableFor(foreignKeys: Seq[ForeignKey]) = table(
    thead(tr(
      td(Messages("th.name")),
      td(Messages("th.source.columns")),
      td(Messages("th.target.table")),
      td(Messages("th.target.columns"))
    )),
    tbody(
      foreignKeys.map { key =>
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
