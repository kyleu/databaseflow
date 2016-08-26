package services.charting

import models.template.ChartOptionsTemplate
import org.scalajs.dom
import org.scalajs.dom.UIEvent
import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js

object ChartingService {
  private[this] var plotly: Option[js.Dynamic] = None
  private[this] val strippedTitles = Seq(
    "Save and edit plot in cloud", "Produced with Plotly",
    "Toggle show closest data on hover", "Show closest data on hover", "Compare data on hover",
    "Box Select", "Lasso Select", "Reset axes", "Reset camera to last save"
  )
  private[this] var activeCharts = Seq.empty[(String, dom.Element)]

  def init() = {
    plotly = Some(js.Dynamic.global.Plotly)
    dom.window.onresize = (ev: UIEvent) => activeCharts.foreach(x => plotly.map(_.Plots.resize(x._2)))
    utils.Logging.info("Charting initialized.")
  }

  def start(el: String) = {
    utils.Logging.info(el)
  }

  def renderOptions(el: String, columns: js.Array[js.Object], chart: js.Object) = {
    $("#" + el).html(ChartOptionsTemplate.forChart(columns, chart).toString)
  }

  def render(el: String, columns: js.Array[js.Object], data: js.Array[js.Array[String]], chart: js.Object) = {
    val chartData: Seq[js.Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array(1, 2, 3, 4, 5, 6),
        "y" -> js.Array(1, 2, 4, 8, 16, 32)
      )
    )
    val baseOptions = js.Dynamic.literal(
      "margin" -> js.Dynamic.literal("l" -> 0, "r" -> 0, "t" -> 0, "b" -> 0)
    )

    ChartingService.addChart(el, chartData, baseOptions)
  }

  def addChart(id: String, data: Seq[js.Dynamic], options: js.Dynamic) = {
    val el = dom.document.getElementById(id)
    plotly.map(_.plot(el, js.Array(data: _*), options))
    $(".modebar-btn", el).each { (x: Int, y: dom.Element) =>
      val jq = $(y)
      if (strippedTitles.contains(jq.data("title").toString)) {
        jq.remove()
      }
    }
    activeCharts = activeCharts :+ (id -> el)
  }

  def removeChart(id: String) = {
    activeCharts = activeCharts.filterNot(_._1 == id)
  }
}
