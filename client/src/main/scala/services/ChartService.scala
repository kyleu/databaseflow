package services

import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js

object ChartService {
  private[this] var chartingScriptLoaded = false
  private[this] var plotlyScriptLoaded = false
  private[this] var plotly3dScriptLoaded = false

  private[this] val scriptRoutes = js.Dynamic.global.scriptRoutes

  private[this] def loadScript(url: String, callback: () => Unit) = {
    utils.Logging.info(s"Loading charting script from [$url].")
    $.getScript(url, () => {
      utils.Logging.info(s"Successfully loaded charting script from [$url].")
      callback()
    })
  }

  def loadChartingScript(onSuccess: () => Unit) = if (chartingScriptLoaded) {
    onSuccess()
  } else {
    loadScript(scriptRoutes.charting.toString, () => {
      chartingScriptLoaded = true
      onSuccess()
    })
  }

  def loadPlotlyScript(onSuccess: () => Unit) = if (plotlyScriptLoaded) {
    onSuccess()
  } else {
    loadScript(scriptRoutes.plotly.toString, () => {
      plotlyScriptLoaded = true
      onSuccess()
    })
  }

  def loadPlotly3dScript(onSuccess: () => Unit) = if (plotly3dScriptLoaded) {
    onSuccess()
  } else {
    loadScript(scriptRoutes.plotly3d.toString, () => {
      plotly3dScriptLoaded = true
      onSuccess()
    })
  }

  def init() = {
    $.ajaxSetup(js.Dynamic.literal(
      "cache" -> true
    ))

    val success = () => {
      utils.Logging.info("Callback!")
    }

    loadChartingScript(success)
    loadPlotlyScript(success)
    loadPlotly3dScript(success)
  }
}
