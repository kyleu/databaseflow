package services.charting

import scala.scalajs.js
import org.scalajs.jquery.{jQuery => $}

object ChartingTests {
  private[this] val baseOptions = js.Dynamic.literal(
    "margin" -> js.Dynamic.literal("l" -> 0, "r" -> 0, "t" -> 0, "b" -> 0)
  )

  def getEl(id: String) = $(s"#chart-$id .chart-panel").get(0)

  def testLineChart(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "x" -> js.Array(1, 2, 3, 4, 5, 6),
    "y" -> js.Array(1, 2, 4, 8, 16, 32)
  )), baseOptions)

  def testBarChart(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "x" -> js.Array("One", "Two", "Three", "Four", "Five", "Six"),
    "y" -> js.Array(1, 2, 4, 8, 16, 32),
    "type" -> "bar"
  )), baseOptions)

  def testPieChart(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "values" -> js.Array(1, 2, 3, 4, 5, 6),
    "labels" -> js.Array("One", "Two", "Three", "Four", "Five", "Six"),
    "type" -> "pie"
  )), baseOptions)

  def testScatterPlot(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "x" -> js.Array(1, 2, 3, 4, 5, 6),
    "y" -> js.Array(1, 2, 4, 8, 16, 32),
    "mode" -> "markers",
    "type" -> "scatter",
    "text" -> js.Array("One", "Two", "Three", "Four", "Five", "Six")
  )), baseOptions)

  def testBubbleChart(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "x" -> js.Array(1, 2, 3, 4, 5, 6),
    "y" -> js.Array(1, 2, 4, 8, 16, 32),
    "mode" -> "markers",
    "type" -> "scatter",
    "marker" -> js.Dynamic.literal(
      "size" -> js.Array(10, 20, 30, 40, 50, 60)
    )
  )), baseOptions)

  def testHistogram(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "x" -> js.Array(1, 2, 3, 4, 5, 6),
    "type" -> "histogram",
    "name" -> js.Array("One", "Two", "Three", "Four", "Five", "Six")
  )), baseOptions)

  def testBoxPlot(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "y" -> js.Array(1, 2, 3, 4, 5, 6),
    "type" -> "box"
  )), baseOptions)

  def testScatterPlot3D(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "x" -> js.Array(1, 2, 3, 4, 5, 6),
    "y" -> js.Array(1, 2, 4, 8, 16, 32),
    "z" -> js.Array(1, 2, 4, 8, 16, 32),
    "mode" -> "markers",
    "type" -> "scatter3d"
  )), baseOptions)

  def testBubbleChart3D(el: String) = ChartRenderService.renderChart(getEl(el), Seq(js.Dynamic.literal(
    "x" -> js.Array(1, 2, 3, 4, 5, 6),
    "y" -> js.Array(1, 2, 4, 8, 16, 32),
    "z" -> js.Array(1, 2, 4, 8, 16, 32),
    "size" -> js.Array(10, 20, 34, 58, 76, 82),
    "mode" -> "markers",
    "type" -> "scatter3d"
  )), baseOptions)
}
