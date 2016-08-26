package services

import java.util.UUID

import models.ChartDataRequest
import models.query.QueryResult
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.{NetworkMessage, ScriptLoader}

import scala.scalajs.js

object ChartService {
  private[this] val cache = collection.mutable.HashMap.empty[UUID, (UUID, QueryResult.Source, Option[Seq[Seq[Option[String]]]])]
  private[this] var charting: Option[js.Dynamic] = None

  def showChart(resultId: UUID, queryId: UUID, source: QueryResult.Source, panel: JQuery) = {
    $(".results-data-panel", panel).hide()
    $(".results-chart-panel", panel).show()

    cache.get(resultId) match {
      case Some(_) => // No op
      case None => startChart(resultId, queryId, source)
    }
  }

  private[this] def startChart(resultId: UUID, queryId: UUID, source: QueryResult.Source): Unit = {
    cache(resultId) = (queryId, source, None)
    NetworkMessage.sendMessage(ChartDataRequest(resultId, source))

    charting match {
      case Some(c) =>

      case None =>
        val chartingLoadSuccess = () => {
          utils.Logging.info("Charting script loaded.")
          charting = Some(js.Dynamic.global.Charting)
          js.timers.setTimeout(1000)(loadPlotly())
          startChart(resultId, queryId, source)
        }
        ScriptLoader.loadScript("charting", chartingLoadSuccess)
    }
  }

  def closeCharts(queryId: UUID) = cache.filter(_._1 == queryId).foreach { resultId =>
    cache.remove(resultId._1)
  }

  def showData(resultId: UUID, panel: JQuery) = {
    $(".results-chart-panel", panel).hide()
    $(".results-data-panel", panel).show()
  }

  private[this] def loadPlotly() = {
    ScriptLoader.loadScript("plotly", () => utils.Logging.info("Plotly script loaded."))
    ScriptLoader.loadScript("plotly3d", () => utils.Logging.info("Plotly3D script loaded."))
  }
}
