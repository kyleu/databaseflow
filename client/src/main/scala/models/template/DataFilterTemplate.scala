package models.template

import java.util.UUID

import models.query.QueryResult
import models.schema.FilterOp

import scalatags.Text.all._

object DataFilterTemplate {
  def forResults(r: QueryResult, resultId: UUID) = if (r.isStatement || r.data.isEmpty) {
    div()
  } else {
    val source = r.source.getOrElse(throw new IllegalStateException(s"Missing source for row data, result [$resultId]"))
    val hiddenClass = source.sortedColumn match {
      case Some(_) => ""
      case None => "initially-hidden"
    }
    div(cls := s"filter-container $hiddenClass z-depth-1")(
      div(cls := "row")(
        div(cls := "input-field col s5")(
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
        div(cls := "input-field col s5")(
          input(name := "", `type` := "text", cls := "theme-text"),
          label("Value")
        )
      ),
      div(cls := "row")(
        div(cls := "col s12")(
          button(cls := "btn theme right waves-effect waves-light")("Filter"),
          button(cls := "btn-flat waves-effect waves-light right results-filter-cancel")("Cancel")
        )
      )
    )
  }
}
