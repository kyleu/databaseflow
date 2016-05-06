package services

import models.QueryErrorResponse
import models.template.ErrorTemplate
import ui.ProgressManager

object QueryErrorService {
  def handleQueryErrorResponse(qer: QueryErrorResponse): Unit = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = ErrorTemplate.forQueryError(qer, occurred.toISOString, occurred.toString)
    ProgressManager.completeProgress(qer.error.queryId, qer.id, content)
  }
}
