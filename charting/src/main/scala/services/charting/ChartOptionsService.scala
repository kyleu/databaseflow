package services.charting

import models.template.ChartOptionsTemplate
import org.scalajs.jquery.{JQuery}

import scala.scalajs.js

object ChartOptionsService {
  def renderOptions(el: JQuery, columns: Seq[(String, String)], chart: js.Dynamic) = {
    el.html(ChartOptionsTemplate.forChart(columns, chart).toString)
    js.Dynamic.global.$("select", el).material_select()
  }
}
