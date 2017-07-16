package models.template.query

import models.query.QueryResult
import models.template.Icons
import util.Messages

import scalatags.Text.all._

object QueryFilterTemplate {
  def activeFilterPanel(qr: QueryResult) = div(qr.source.flatMap(_.filterOpt) match {
    case Some(filter) =>
      div(cls := "active-filter z-depth-1")(
        div(cls := "filter-cancel-link")(i(cls := "theme-text fa " + Icons.close)),
        i(cls := "fa " + Icons.filter),
        Messages("query.active.filter"),
        ": ",
        strong(filter.col),
        s" ${filter.op.symbol} ",
        strong(filter.v)
      )
    case None => ""
  })
}
