import services.charting.{ChartingService, ChartingTests}

import scala.scalajs.js.annotation.JSExport

@JSExport
object Charting {
  @JSExport
  def init() = {
    ChartingService.init()
  }

  @JSExport
  def test(key: String) = key match {
    case "line" => ChartingTests.testLineChart()
    case "bar" => ChartingTests.testBarChart()
    case "pie" => ChartingTests.testPieChart()
    case "scatter" => ChartingTests.testScatterChart()
    case "bubble" => ChartingTests.testBubbleChart()
    case "3d" => ChartingTests.test3DScatterChart()
  }
}
