import java.util.UUID

import models.charting.ChartSettings
import services.charting.{ChartingService, ChartingTests}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel(name = "Charting")
object Charting {
  @JSExport
  def init() = ChartingService.init()

  @JSExport
  def addChart(id: String, settings: js.Dynamic, columns: js.Array[js.Dynamic], data: js.Array[js.Array[String]]) = {
    val uuid = UUID.fromString(id)
    val cs = ChartSettings.fromJs(settings)
    val colSeq = toColumns(columns)
    ChartingService.addChart(uuid, cs, colSeq, data)
  }

  @JSExport
  def getSettingsString(id: String) = ChartingService.getSettingsOpt(UUID.fromString(id)).map(_.asJson).getOrElse("")

  @JSExport
  def updateData(id: String, data: js.Array[js.Array[String]]) = ChartingService.updateData(UUID.fromString(id), data)

  @JSExport
  def test(el: String, key: String) = key match {
    case "line" => ChartingTests.testLineChart(el)
    case "bar" => ChartingTests.testBarChart(el)
    case "pie" => ChartingTests.testPieChart(el)
    case "scatter" => ChartingTests.testScatterPlot(el)
    case "bubble" => ChartingTests.testBubbleChart(el)
    case "histogram" => ChartingTests.testHistogram(el)
    case "box" => ChartingTests.testBoxPlot(el)
    case "scatter3d" => ChartingTests.testScatterPlot3D(el)
    case "bubble3d" => ChartingTests.testBubbleChart3D(el)
    case _ => throw new IllegalStateException(s"Invalid chart type [$key].")
  }

  private[this] def toColumns(columns: js.Array[js.Dynamic]) = columns.map(col => col.name.toString -> col.t.toString).toSeq
}
