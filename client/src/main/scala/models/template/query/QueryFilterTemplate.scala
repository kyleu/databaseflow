package models.template.query

import java.util.UUID

import models.query.QueryResult
import models.schema.FilterOp
import models.template.results.{DataFilterTemplate, DataTableTemplate}
import models.template.{Icons, StaticPanelTemplate}
import utils.{Messages, NumberUtils, TemplateUtils}

import scalatags.Text.all._

object QueryFilterTemplate {
  def activeFilterPanel(qr: QueryResult) = div(qr.source.flatMap(_.filterColumn) match {
    case Some(column) =>
      val op = qr.source.flatMap(_.filterOp).getOrElse(FilterOp.Equal).symbol
      val v = qr.source.flatMap(_.filterValue).getOrElse("?")
      div(cls := "active-filter z-depth-1")(
        div(cls := "filter-cancel-link")(i(cls := "theme-text fa " + Icons.close)),
        i(cls := "fa " + Icons.filter),
        Messages("query.active.filter"),
        ": ",
        strong(column),
        s" $op ",
        strong(v)
      )
    case None => ""
  })
}
