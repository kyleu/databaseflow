package services.charting

import models.charting.ChartType
import models.template.ChartOptionsTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object ChartOptionsService {
  private[this] def materialSelect(selector: String, el: JQuery) = js.Dynamic.global.$(selector, el).material_select()

  private[this] def selectChartType(el: JQuery, columns: Seq[(String, String)], t: ChartType) = {
    $(".chart-columns-container", el).html(ChartOptionsTemplate.selects(t, columns.map(c => c._1 -> c._1)).toString())
    materialSelect(".chart-select", el)

    $(".chart-flags-container", el).html(ChartOptionsTemplate.flags(t).toString())
  }

  def renderOptions(el: JQuery, columns: Seq[(String, String)], chart: js.Dynamic) = {
    el.html(ChartOptionsTemplate.options(columns, chart).toString)
    materialSelect(".chart-type-select", el)
    TemplateUtils.changeHandler($(".chart-type-select", el), (jq) => {
      val key = jq.value().toString
      utils.Logging.info(key)
      selectChartType(el, columns, ChartType.withName(key))
    })
    selectChartType(el, columns, ChartType.Line)
  }
}
