package services.charting

import java.util.UUID

import models.charting.{ChartSettings, ChartType}
import models.template.ChartOptionsTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import util.TemplateUtils

import scala.scalajs.js

object ChartSettingsService {
  private[this] def materialSelect(selector: String, el: JQuery) = js.Dynamic.global.$(selector, el).material_select()

  private[this] def onSelectChange(id: UUID, key: String, value: String) = {
    val settings = ChartingService.getSettings(id)
    val updated = if (value.trim.isEmpty) {
      settings.copy(selects = settings.selects.filterNot(_._1 == key))
    } else {
      settings.copy(selects = settings.selects + (key -> value))
    }
    ChartingService.updateSettings(id, updated)
  }

  private[this] def onFlagChange(id: UUID, key: String, value: Boolean) = {
    val settings = ChartingService.getSettings(id)
    val updated = settings.copy(flags = settings.flags + (key -> value))
    ChartingService.updateSettings(id, updated)
  }

  private[this] def selectChartType(id: UUID, el: JQuery, columns: Seq[(String, String)], chart: ChartSettings) = {
    $(".chart-columns-container", el).html(ChartOptionsTemplate.selects(chart, columns.map(c => c._1 -> c._1)).toString())
    materialSelect(".chart-select", el)
    TemplateUtils.changeHandler($(".chart-select", el), jq => onSelectChange(id, jq.data("key").toString, jq.value().toString))

    $(".chart-flags-container", el).html(ChartOptionsTemplate.flags(id, chart).toString())
    TemplateUtils.changeHandler($(".chart-flag", el), jq => onFlagChange(id, jq.data("key").toString, jq.prop("checked").toString.toBoolean))

    ChartingService.updateSettings(id, chart)
  }

  def renderOptions(id: UUID, panel: JQuery, columns: Seq[(String, String)]) = {
    val chart = ChartingService.getSettings(id)
    panel.html(ChartOptionsTemplate.options(columns, chart).toString)
    materialSelect(".chart-type-select", panel)
    TemplateUtils.changeHandler($(".chart-type-select", panel), jq => {
      val currentSettings = ChartingService.getSettings(id)
      val newSettings = ChartType.withName(jq.value().toString).defaultSettings.merge(currentSettings)
      selectChartType(id, panel, columns, newSettings)
    })
    selectChartType(id, panel, columns, chart)
  }
}
