package models.template

import models.schema.ForeignKey

import scalatags.Text.all._

object TableForeignKeyDetailTemplate {
  def foreignKeyPanel(foreignKeys: Seq[ForeignKey]) = {
    tableFor(foreignKeys)
  }

  private[this] def tableFor(foreignKeys: Seq[ForeignKey]) = table(
    thead(tr(
      td("Name"),
      td("Source Columns"),
      td("Target Table"),
      td("Target Columns")
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
