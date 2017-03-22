package services.query

import models.template.query.QueryErrorTemplate
import models.{QueryCheckResponse, QueryErrorResponse}
import org.scalajs.jquery.{jQuery => $}
import ui.ProgressManager
import ui.editor.EditorManager
import utils.TemplateUtils

object QueryErrorService {
  def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = QueryErrorTemplate.forQueryError(qer, occurred.toISOString)
    ProgressManager.completeProgress(qer.error.queryId, qer.id, qer.index, content)

    val panel = $("#" + qer.id)
    val sqlEl = $(".query-result-sql", panel)
    var sqlShown = false
    TemplateUtils.clickHandler($(".results-sql-link", panel), jq => {
      if (sqlShown) { sqlEl.hide() } else { sqlEl.show() }
      sqlShown = !sqlShown
    })
  }

  def handleQueryCheckResponse(qcr: QueryCheckResponse) = {
    EditorManager.highlightErrors(qcr.queryId, qcr.results)
  }
}
