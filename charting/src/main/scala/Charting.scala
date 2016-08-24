import services.charting.{ChartingService, ChartingTests}

import scala.scalajs.js.annotation.JSExport

@JSExport
class Charting {
  ChartingService.init()

  //ChartingTests.testLineChart()
  //ChartingTests.testBubbleChart()
  //ChartingTests.testScatterChart()
  ChartingTests.test3DScatterChart()
  //ChartingTests.testBarChart()
  //ChartingTests.testPieChart()
}
