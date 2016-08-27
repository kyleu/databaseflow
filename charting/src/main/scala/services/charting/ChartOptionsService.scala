package services.charting

import java.util.UUID

import models.charting.{ChartSettings, ChartType}
import models.template.ChartOptionsTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object ChartOptionsService {
  private[this] def materialSelect(selector: String, el: JQuery) = js.Dynamic.global.$(selector, el).material_select()

  private[this] def selectChartType(id: UUID, el: JQuery, columns: Seq[(String, String)], t: ChartType) = {
    $(".chart-columns-container", el).html(ChartOptionsTemplate.selects(t, columns.map(c => c._1 -> c._1)).toString())
    materialSelect(".chart-select", el)
    TemplateUtils.changeHandler($(".chart-select", el), (jq) => {
      val k = jq.data("key").toString
      val v = jq.value().toString
      utils.Logging.info(k + ": " + v)
    })
    $(".chart-flags-container", el).html(ChartOptionsTemplate.flags(id, t).toString())
  }

  def renderOptions(id: UUID, panel: JQuery, columns: Seq[(String, String)], chart: ChartSettings) = {
    panel.html(ChartOptionsTemplate.options(columns, chart).toString)
    materialSelect(".chart-type-select", panel)
    TemplateUtils.changeHandler($(".chart-type-select", panel), (jq) => {
      selectChartType(id, panel, columns, ChartType.withName(jq.value().toString))
    })
    selectChartType(id, panel, columns, ChartType.Line)
  }
}
