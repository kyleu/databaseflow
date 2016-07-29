package services

import java.util.UUID

import models.template.query.QueryErrorTemplate
import models.{QueryCheckResponse, QueryErrorResponse}
import ui.{EditorManager, ProgressManager}

object QueryErrorService {
  def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = QueryErrorTemplate.forQueryError(qer, occurred.toISOString)
    ProgressManager.completeProgress(qer.error.queryId, qer.id, content)
  }

  def handleQueryCheckResponse(qcr: QueryCheckResponse) = qcr.error match {
    case Some(err) => EditorManager.highlightError(qcr.queryId, qcr.sql, err, qcr.line, qcr.position)
    case None => EditorManager.clearError(qcr.queryId)
  }
}
