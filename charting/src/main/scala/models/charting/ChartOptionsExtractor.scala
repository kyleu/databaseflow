package models.charting

import scala.scalajs.js

object ChartOptionsExtractor {
  def getJsOptions(settings: ChartSettings) = settings.t match {
    case ChartType.Line => js.Dynamic.literal(
      "margin" -> js.Dynamic.literal("l" -> 0, "r" -> 0, "t" -> 0, "b" -> 0)
    )
    case x => throw new IllegalArgumentException(x.id)
  }
}
