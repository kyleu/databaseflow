package models.template.proc

import models.schema.{Column, ProcedureParam}
import utils.Messages

import scalatags.Text.all._

object ProcedureParameterDetailTemplate {
  def paramsPanel(params: Seq[ProcedureParam]) = {
    tableFor(params)
  }

  private[this] def tableFor(params: Seq[ProcedureParam]) = table(cls := "bordered highlight responsive-table")(
    thead(tr(
      th(Messages("th.name")),
      th(Messages("th.description")),
      th(Messages("th.type")),
      th(Messages("th.column"))
    )),
    tbody(params.map { p =>
      tr(
        td(p.name),
        td(p.description.getOrElse(""): String),
        td(p.paramType),
        td(p.columnType.toString)
      )
    })
  )
}
