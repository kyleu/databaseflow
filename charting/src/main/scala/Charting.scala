import services.charting.{ChartingService, ChartingTests}

import scala.scalajs.js.annotation.JSExport

@JSExport
object Charting {
  @JSExport
  def init() = {
    ChartingService.init()
  }

  @JSExport
  def start(el: String) = {
    ChartingService.start(el)
  }

  @JSExport
  def test(el: String, key: String) = key match {
    case "line" => ChartingTests.testLineChart(el)
    case "bar" => ChartingTests.testBarChart(el)
    case "pie" => ChartingTests.testPieChart(el)
    case "scatter" => ChartingTests.testScatterChart(el)
    case "bubble" => ChartingTests.testBubbleChart(el)
    case "3d" => ChartingTests.test3DScatterChart(el)
  }
}
