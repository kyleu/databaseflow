package services.charting

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js

object ChartRenderService {
  private[this] var plotly: Option[js.Dynamic] = None
  private[this] val strippedTitles = Seq(
    "Save and edit plot in cloud", "Produced with Plotly",
    "Toggle show closest data on hover", "Show closest data on hover", "Compare data on hover",
    "Box Select", "Lasso Select", "Reset axes", "Reset camera to last save"
  )

  def init() = plotly = Some(js.Dynamic.global.Plotly)
  def resizeHandler(el: Element) = plotly.map(_.Plots.resize(el))

  def render(v: ChartingService.ChartValues) = renderChart(v.chartPanel.get(0), v.chartData, v.baseOptions)

  def renderChart(el: Element, data: Seq[js.Dynamic], options: js.Dynamic) = {
    //utils.Logging.logJs(js.Array(data: _*))
    plotly.map(_.newPlot(el, js.Array(data: _*), options))
    $(".modebar-btn", el).each { (_: Int, y: dom.Element) =>
      val jq = $(y)
      if (strippedTitles.contains(jq.data("title").toString)) {
        jq.remove()
      }
    }
  }
}
