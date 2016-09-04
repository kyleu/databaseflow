package models.template.query

import java.util.UUID

import models.query.QueryResult
import models.template.StaticPanelTemplate
import models.template.results.{ChartResultTemplate, DataFilterTemplate, DataTableTemplate}
import utils.{Messages, NumberUtils, TemplateUtils}

import scalatags.Text.all._

object QueryResultsTemplate {
  def forQueryResults(qr: QueryResult, dateIsoString: String, durationMs: Int, resultId: UUID, chartId: UUID) = {
    val hasFilter = !(qr.isStatement || qr.data.isEmpty || qr.source.isEmpty)

    val sqlPre = div(cls := "z-depth-1 query-result-sql")(
      pre(cls := "pre-wrap")(qr.sql)
    )

    val dataPanel = div(cls := "results-data-panel")(
      DataTableTemplate.forResults(qr, resultId),
      em(cls := "right")(
        s"${NumberUtils.withCommas(qr.rowsAffected)} ",
        span(cls := "total-row-count"),
        " rows returned ",
        TemplateUtils.toTimeago(dateIsoString),
        " in ",
        span(cls := "total-duration")(NumberUtils.withCommas(durationMs)),
        "ms"
      ),

      div(cls := "additional-results")(
        a(cls := "append-rows-link theme-text initially-hidden", data("offset") := "0", data("limit") := qr.data.size.toString, href := "#")(
          Messages("query.load.more", utils.NumberUtils.withCommas(qr.data.size))
        ),
        em(cls := "no-rows-remaining initially-hidden")(Messages("query.no.more.rows"))
      )
    )

    val content = if (hasFilter) {
      div(id := s"$resultId")(
        QueryFilterTemplate.activeFilterPanel(qr),
        div(
          a(href := "#", cls := "results-share-link results-nav-link right theme-text")(Messages("query.share")),
          a(href := "#", cls := "results-export-link results-nav-link right theme-text")(Messages("query.export")),
          a(href := "#", cls := "results-filter-link results-nav-link right theme-text")(Messages("th.filter")),
          a(href := "#", cls := "results-sql-link results-nav-link right theme-text")(Messages("th.sql")),
          div(cls := "left")(
            div(cls := "switch")(
              label(
                Messages("query.data"),
                input(cls := "results-chart-toggle", `type` := "checkbox"),
                span(cls := "lever"),
                Messages("query.chart")
              )
            )
          )
        ),
        div(cls := "clear"),
        DataFilterTemplate.forResults(qr, resultId),
        sqlPre,
        dataPanel,
        ChartResultTemplate.forChartResults(chartId)
      )
    } else {
      div(id := s"$resultId")(
        QueryFilterTemplate.activeFilterPanel(qr),
        div(
          a(href := "#", cls := "results-sql-link results-nav-link right theme-text")(Messages("th.sql"))
        ),
        div(cls := "clear"),
        sqlPre,
        dataPanel,
        ChartResultTemplate.forChartResults(chartId)
      )
    }

    StaticPanelTemplate.cardRow(
      content = content,
      showClose = false
    )
  }
}
