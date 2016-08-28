package models.charting

import scala.scalajs.js

object ChartOptionsExtractor {
  def getJsOptions(settings: ChartSettings) = settings.t match {
    case ChartType.Line => js.Dynamic.literal(
      "margin" -> js.Dynamic.literal("t" -> 40),
      "xaxis" -> js.Dynamic.literal("title" -> (settings.selects.getOrElse("x", ""): String)),
      "yaxis" -> js.Dynamic.literal("title" -> (settings.selects.getOrElse("y", ""): String))
    )
    case x => throw new IllegalArgumentException(x.id)
  }
}
