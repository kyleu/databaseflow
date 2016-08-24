package services.charting

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
  }

  def addChart(id: String, data: Seq[js.Dynamic], options: js.Dynamic) = {
    val el = dom.document.getElementById("chart")
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
