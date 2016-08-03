package services

import models.template.query.QueryErrorTemplate
import models.{QueryCheckResponse, QueryErrorResponse}
import ui.{EditorManager, ProgressManager}

object QueryErrorService {
  def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = QueryErrorTemplate.forQueryError(qer, occurred.toISOString)
    ProgressManager.completeProgress(qer.error.queryId, qer.id, qer.index, content)
  }

  def handleQueryCheckResponse(qcr: QueryCheckResponse) = {
    EditorManager.highlightErrors(qcr.queryId, qcr.results)
  }
}
