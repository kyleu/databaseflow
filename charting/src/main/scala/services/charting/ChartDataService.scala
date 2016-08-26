package services.charting

import models.template.ChartOptionsTemplate
import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js

object ChartDataService {
  def renderOptions(el: String, columns: js.Array[js.Object], chart: js.Object) = {
    $("#" + el).html(ChartOptionsTemplate.forChart(columns, chart).toString)
  }
}
