package services

import java.util.UUID

import models.template.query.QueryErrorTemplate
import models.{ QueryCheckResponse, QueryErrorResponse }
import ui.ProgressManager

object QueryErrorService {
  def handleQueryCheckResponse(qcr: QueryCheckResponse) = qcr.error match {
    case Some(err) => handleError(qcr.queryId, err, qcr.line, qcr.position)
    case None => handleSuccess(qcr.queryId)
  }

  def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = QueryErrorTemplate.forQueryError(qer, occurred.toISOString)
    ProgressManager.completeProgress(qer.error.queryId, qer.id, content)
  }

  def handleError(queryId: UUID, err: String, line: Option[Int], position: Option[Int]) = {
    utils.Logging.debug(s"Query error [$err] at [${line.getOrElse("?")}:${position.getOrElse("?")}] for query [$queryId].")
  }

  def handleSuccess(queryId: UUID) = {
    utils.Logging.debug(s"Query [$queryId] was checked successfully.")
  }
}
