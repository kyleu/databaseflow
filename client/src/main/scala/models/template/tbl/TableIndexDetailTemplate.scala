package models.template.tbl

import models.schema.Index
import utils.Messages

import scalatags.Text.all._

object TableIndexDetailTemplate {
  def indexPanel(indexes: Seq[Index]) = {
    tableFor(indexes)
  }

  private[this] def tableFor(indexes: Seq[Index]) = table(cls := "bordered highlight responsive-table")(
    thead(
      tr(
        th(Messages("th.name")),
        th(Messages("th.unique")),
        th(Messages("th.type")),
        th(Messages("th.columns"))
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
