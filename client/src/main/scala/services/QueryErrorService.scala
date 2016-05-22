package services

import models.{ QueryCheckResponse, QueryErrorResponse }
import models.template.ErrorTemplate
import ui.ProgressManager

object QueryErrorService {
  def handleQueryCheckResponse(qcr: QueryCheckResponse) = qcr.error match {
    case Some(err) => utils.Logging.info(s"Query error [$err] at [${qcr.location}] for query [${qcr.queryId}].")
    case None => utils.Logging.info(s"Query [${qcr.queryId}] checked successfully.")
  }

  def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = ErrorTemplate.forQueryError(qer, occurred.toISOString)
    ProgressManager.completeProgress(qer.error.queryId, qer.id, content)
  }
}
