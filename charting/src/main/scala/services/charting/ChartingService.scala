package services.charting

import java.util.UUID

import models.charting.ChartSettings
import org.scalajs.dom
import org.scalajs.dom.UIEvent
import org.scalajs.jquery.{JQuery, jQuery => $}

import scala.scalajs.js
import scala.scalajs.js.{Array, Dynamic}

object ChartingService {
  case class ChartValues(optionsPanel: JQuery, chartPanel: JQuery, settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) {
    lazy val chartData: Seq[Dynamic] = Seq(
      js.Dynamic.literal(
        "x" -> js.Array(1, 2, 3, 4, 5, 6),
        "y" -> js.Array(1, 2, 4, 8, 16, 32)
      )
    )
    lazy val baseOptions = js.Dynamic.literal(
      "margin" -> js.Dynamic.literal("l" -> 0, "r" -> 0, "t" -> 0, "b" -> 0)
    )
  }

  private[this] var activeCharts = Map.empty[UUID, ChartValues]

  def init() = {
    ChartRenderService.init()
    dom.window.onresize = (ev: UIEvent) => activeCharts.mapValues(x => ChartRenderService.resizeHandler(x.chartPanel.get(0)))
    utils.Logging.info("Charting initialized.")
  }

  def addChart(id: UUID, settings: ChartSettings, columns: Seq[(String, String)], data: Array[Array[String]]) = {
    val el = $(s"#chart-$id")
    if (el.length != 1) {
      throw new IllegalStateException(s"Missing element for chart [$id].")
    }
    val optionsPanel = $(".chart-options-panel", el)
    if (optionsPanel.length != 1) {
      throw new IllegalStateException(s"Missing options panel for chart [$id].")
    }

    val chartPanel = $(".chart-panel", el)
    if (chartPanel.length != 1) {
      throw new IllegalStateException(s"Missing chart panel for chart [$id].")
    }

    val v = ChartValues(optionsPanel, chartPanel, settings, columns, data)
    activeCharts = activeCharts + (id -> v)
    ChartOptionsService.renderOptions(id, optionsPanel, columns, settings)
    ChartRenderService.render(v)
  }

  def removeChart(id: UUID) = {
    activeCharts = activeCharts - id
  }
}
