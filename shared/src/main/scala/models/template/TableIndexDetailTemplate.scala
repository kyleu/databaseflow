package models.template

import models.schema.Index

import scalatags.Text.all._

object TableIndexDetailTemplate {
  def indexPanel(indexes: Seq[Index]) = {
    tableFor(indexes)
  }

  private[this] def tableFor(indexes: Seq[Index]) = table(cls := "bordered highlight responsive-table")(
    thead(
      tr(
        th("Name"),
        th("Unique"),
        th("Type"),
        th("Columns")
      )
    ),
    tbody(
      indexes.map { idx =>
        val uniq = idx.unique.toString
        tr(
          td(idx.name),
          td(uniq),
          td(idx.indexType),
          td(idx.columns.mkString(", "))
        )
      }
    )
  )
}
