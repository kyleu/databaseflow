package services

import java.util.UUID

import models.ChartDataRequest
import models.query.QueryResult
import utils.{NetworkMessage, ScriptLoader}

import scala.scalajs.js

object ChartService {
  private[this] val cache = collection.mutable.HashMap.empty[UUID, (QueryResult.Source, Option[Seq[Seq[Option[String]]]])]
  private[this] var charting: Option[js.Dynamic] = None

  def startChart(id: UUID, source: QueryResult.Source): Unit = {
    cache(id) = source -> None
    NetworkMessage.sendMessage(ChartDataRequest(id, source))

    charting match {
      case Some(c) => // TODO
      case None =>
        val chartingLoadSuccess = () => {
          utils.Logging.info("Charting script loaded.")
          charting = Some(js.Dynamic.global.Charting)
          js.timers.setTimeout(1000)(loadPlotly())
          startChart(id, source)
        }
        ScriptLoader.loadScript("charting", chartingLoadSuccess)
    }
  }

  private[this] def loadPlotly() = {
    ScriptLoader.loadScript("plotly", () => utils.Logging.info("Plotly script loaded."))
    ScriptLoader.loadScript("plotly3d", () => utils.Logging.info("Plotly3D script loaded."))
  }
}
