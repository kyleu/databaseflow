package models.template.results

import java.util.UUID

import models.query.QueryResult
import models.schema.FilterOp
import utils.Messages

import scalatags.Text.all._

object DataFilterTemplate {
  def forResults(r: QueryResult, resultId: UUID) = {
    val source = r.source.getOrElse(throw new IllegalStateException(s"Missing source for row data, result [$resultId]."))
    div(cls := "filter-container initially-hidden z-depth-1")(
      div(cls := "row")(
        div(cls := "input-field col s4")(
          select(cls := "filter-select filter-col-select theme-text")(r.columns.map(c =>
            if (source.filterOpt.exists(_.col == c.name)) {
              option(selected)(c.name)
            } else {
              option(c.name)
            }))
        ),
        div(cls := "input-field col s2")(
          select(cls := "filter-select filter-op-select theme-text")(FilterOp.values.map { op =>
            if (source.filterOpt.exists(_.op == op)) {
              option(value := op.key, selected)(op.symbol)
            } else {
              option(value := op.key)(op.symbol)
            }
          })
        ),
        div(cls := "single-value" + (if (source.filterOpt.exists(_.op == FilterOp.Between)) { " initially-hidden" } else { "" }))(
          div(cls := "input-field col s6")(
            if (source.filterOpt.exists(_.op == FilterOp.Between)) {
              input(cls := "filter-single-val theme-text", `type` := "text")
            } else {
              input(cls := "filter-single-val theme-text", `type` := "text", value := source.filterOpt.map(_.v).getOrElse(""))
            }
          )
        ),
        div(cls := "double-value" + (if (source.filterOpt.exists(_.op == FilterOp.Between)) { "" } else { " initially-hidden" }))(
          div(cls := "input-field col s3")(
            if (source.filterOpt.exists(_.op == FilterOp.Between)) {
              input(cls := "filter-double-val-a theme-text", `type` := "text", value := source.filterOpt.map(_.v).getOrElse("|").split('|')(0))
            } else {
              input(cls := "filter-double-val-a theme-text", `type` := "text")
            }
          ),
          div(cls := "input-field col s3")(
            if (source.filterOpt.exists(_.op == FilterOp.Between)) {
              input(cls := "filter-double-val-b theme-text", `type` := "text", value := source.filterOpt.map(_.v).getOrElse("|").split('|')(1))
            } else {
              input(cls := "filter-double-val-b theme-text", `type` := "text")
            }
          )
        )
      ),
      div(cls := "row")(
        div(cls := "col s12")(
          button(cls := "btn theme right waves-effect waves-light results-filter-go")(Messages("th.filter")),
          button(cls := "btn-flat right results-filter-cancel")(Messages("th.cancel"))
        )
      )
    )
  }
}
