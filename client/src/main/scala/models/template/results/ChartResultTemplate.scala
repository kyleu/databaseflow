package models.template.results

import java.util.UUID

import utils.Messages

import scalatags.Text.all._

object ChartResultTemplate {
  def forChartResults(chartId: UUID) = div(id := chartId.toString, cls := "results-chart-panel initially-hidden")(
    div(cls := "loading")(Messages("query.chart.loading")),
    div(cls := "chart-options-panel z-depth-1 initially-hidden chart-options-padding"),
    div(cls := "chart-container initially-hidden")(
      div(cls := "chart-panel")
    )
  )
}
