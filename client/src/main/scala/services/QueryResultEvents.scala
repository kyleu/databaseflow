package services

import java.util.UUID

import models.query.{QueryResult, SharedResult}
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.UserManager
import ui.modal.{QueryExportFormManager, SharedResultFormManager}
import utils.TemplateUtils

object QueryResultEvents {
  def wire(panel: JQuery, result: QueryResult, resultId: UUID, chartId: UUID) = {
    val src = result.source.getOrElse(throw new IllegalStateException())
    TemplateUtils.clickHandler($(".results-export-link", panel), (jq) => {
      QueryExportFormManager.show(result.queryId, src, "Export")
    })
    TemplateUtils.clickHandler($(".results-share-link", panel), (jq) => {
      SharedResultFormManager.show(SharedResult(
        owner = UserManager.userId.getOrElse(throw new IllegalStateException()),
        connectionId = NavigationService.connectionId,
        source = result.source.getOrElse(throw new IllegalStateException())
      ))
    })
    TemplateUtils.changeHandler($(".results-chart-toggle", panel), (jq) => {
      if (jq.prop("checked").toString == "true") {
        ChartService.showChart(chartId, result.columns, src, panel)
      } else {
        ChartService.showData(panel)
      }
    })
  }
}
