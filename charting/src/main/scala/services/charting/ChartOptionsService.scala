package services.charting

import models.charting.ChartType
import models.template.ChartOptionsTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object ChartOptionsService {
  private[this] def materialSelect(selector: String, el: JQuery) = js.Dynamic.global.$(selector, el).material_select()

  private[this] def selectChartType(elId: String, el: JQuery, columns: Seq[(String, String)], t: ChartType) = {
    $(".chart-columns-container", el).html(ChartOptionsTemplate.selects(t, columns.map(c => c._1 -> c._1)).toString())
    materialSelect(".chart-select", el)
    TemplateUtils.changeHandler($(".chart-select", el), (jq) => {
      val k = jq.data("key").toString
      val v = jq.value().toString
      utils.Logging.info(k + ": " + v)
    })
    $(".chart-flags-container", el).html(ChartOptionsTemplate.flags(elId, t).toString())
  }

  def renderOptions(elId: String, el: JQuery, columns: Seq[(String, String)], chart: js.Dynamic) = {
    el.html(ChartOptionsTemplate.options(elId, columns, chart).toString)
    materialSelect(".chart-type-select", el)
    TemplateUtils.changeHandler($(".chart-type-select", el), (jq) => {
      selectChartType(elId, el, columns, ChartType.withName(jq.value().toString))
    })
    selectChartType(elId, el, columns, ChartType.Line)
  }
}
