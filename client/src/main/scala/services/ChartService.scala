package services

import java.util.UUID

import models.query.QueryResult
import models.{ChartDataRequest, ChartDataResponse}
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.{NetworkMessage, ScriptLoader}

import scala.scalajs.js

object ChartService {
  private[this] val cache = collection.mutable.HashMap.empty[UUID, QueryResult.Source]
  private[this] var charting: Option[js.Dynamic] = None

  def showChart(chartId: UUID, columns: Seq[QueryResult.Col], source: QueryResult.Source, panel: JQuery) = {
    $(".results-data-panel", panel).hide()
    $(".results-chart-panel", panel).show()

    cache.get(chartId) match {
      case Some(_) => // No op
      case None => startChart(chartId, columns, source)
    }
  }

  def handleChartDataResponse(cdr: ChartDataResponse) = charting match {
    case Some(c) =>
      val data = js.Array(cdr.data.map(r => js.Array(r.map(_.orNull): _*)): _*)
      c.updateData(cdr.chartId.toString, data)
    case None => throw new IllegalStateException("Charting is not loaded.")
  }

  private[this] def startChart(chartId: UUID, columns: Seq[QueryResult.Col], source: QueryResult.Source): Unit = {
    cache(chartId) = source

    charting match {
      case Some(c) =>
        val el = $(s"#$chartId")
        $(".loading", el).remove()

        val chart = js.Dynamic.literal(
          "t" -> "line",
          "selects" -> js.Object(),
          "flags" -> js.Object()
        )

        val cols = js.Array(columns.map { col =>
          js.Dynamic.literal("t" -> col.t.key, "name" -> col.name)
        }: _*)

        c.addChart(chartId.toString, chart, cols, js.Array())

        $(".chart-options-panel", el).show()

      case None =>
        val chartingLoadSuccess = () => {
          utils.Logging.info("Charting script loaded.")
          charting = Some(js.Dynamic.global.Charting())
          NetworkMessage.sendMessage(ChartDataRequest(chartId, source))
          loadPlotly()
          startChart(chartId, columns, source)
        }
        ScriptLoader.loadScript("charting", chartingLoadSuccess)
    }
  }

  def closeCharts(queryId: UUID) = cache.filter(_._1 == queryId).foreach { resultId =>
    cache.remove(resultId._1)
  }

  def showData(panel: JQuery) = {
    $(".results-chart-panel", panel).hide()
    $(".results-data-panel", panel).show()
  }

  def getSettings(id: UUID) = charting.map { c =>
    c.getSettingsString(id.toString).toString
  }.getOrElse("")

  private[this] def loadPlotly() = ScriptLoader.loadScript("plotly", () => {
    charting.foreach(_.init())
  })
}
