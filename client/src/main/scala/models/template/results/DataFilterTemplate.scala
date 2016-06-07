package models.template.results

import java.util.UUID

import models.query.QueryResult
import models.schema.FilterOp

import scalatags.Text.all._

object DataFilterTemplate {
  def forResults(r: QueryResult, resultId: UUID) = if (r.isStatement || r.data.isEmpty) {
    div()
  } else {
    val source = r.source.getOrElse(throw new IllegalStateException(s"Missing source for row data, result [$resultId]"))
    val hiddenClass = source.filterColumn match {
      case Some(_) => ""
      case None => "initially-hidden"
    }
    div(cls := s"filter-container $hiddenClass z-depth-1")(
      div(cls := "row")(
        div(cls := "input-field col s4")(
          select(cls := "filter-select filter-col-select theme-text")(r.columns.map(c =>
            if (source.filterColumn.contains(c.name)) {
              option(selected)(c.name)
            } else {
              option(c.name)
            })),
          label("Column")
        ),
        div(cls := "input-field col s2")(
          select(cls := "filter-select filter-op-select theme-text")(FilterOp.values.map { op =>
            if (source.filterOp.contains(op)) {
              option(value := op.key, selected)(op.symbol)
            } else {
              option(value := op.key)(op.symbol)
            }
          }),
          label("Op")
        ),
        div(cls := "single-value")(
          div(cls := "input-field col s6")(
            input(cls := "filter-single-val theme-text", `type` := "text"),
            label("Value")
          )
        ),
        div(cls := "double-value initially-hidden")(
          div(cls := "input-field col s3")(
            input(cls := "filter-double-val-a theme-text", `type` := "text"),
            label("Value 1")
          ),
          div(cls := "input-field col s3")(
            input(cls := "filter-double-val-b theme-text", `type` := "text"),
            label("Value 2")
          )
        )
      ),
      div(cls := "row")(
        div(cls := "col s12")(
          button(cls := "btn theme right waves-effect waves-light results-filter-go")("Filter"),
          button(cls := "btn-flat waves-effect waves-light right results-filter-cancel")("Cancel")
        )
      )
    )
  }
}
