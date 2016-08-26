import services.charting.{ChartingService, ChartingTests}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

@JSExport
object Charting {
  @JSExport
  def init() = ChartingService.init()

  @JSExport
  def start(el: String) = ChartingService.start(el)

  @JSExport
  def renderOptions(el: String, columns: js.Array[js.Object], chart: js.Object) = ChartingService.renderOptions(el, columns, chart)

  @JSExport
  def render(el: String, columns: js.Array[js.Object], data: js.Array[js.Array[String]], chart: js.Object) = ChartingService.render(el, columns, data, chart)

  @JSExport
  def test(el: String, key: String) = key match {
    case "line" => ChartingTests.testLineChart(el)
    case "bar" => ChartingTests.testBarChart(el)
    case "pie" => ChartingTests.testPieChart(el)
    case "scatter" => ChartingTests.testScatterChart(el)
    case "bubble" => ChartingTests.testBubbleChart(el)
    case "scatter3d" => ChartingTests.test3DScatterChart(el)
    case _ => throw new IllegalStateException(s"Invalid chart type [$key].")
  }
}
