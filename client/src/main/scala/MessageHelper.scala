import models._
import services._
import ui.UserManager
import utils.Logging

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {

    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)

    case us: UserSettings => UserManager.onUserSettings(us)

    case sqrr: SavedQueryResultResponse => ModelResultsService.handleSavedQueryResponse(sqrr)

    case srr: SchemaResultResponse => ModelResultsService.handleSchemaResultResponse(srr)
    case trr: TableResultResponse => ModelResultsService.handleTableResultResponse(trr.tables)
    case vrr: ViewResultResponse => ModelResultsService.handleViewResultResponse(vrr.views)
    case prr: ProcedureResultResponse => ModelResultsService.handleProcedureResultResponse(prr.procedures)

    case qrr: QueryResultResponse => handleQueryResultResponse(qrr)
    case qer: QueryErrorResponse => QueryErrorService.handleQueryErrorResponse(qer)

    case prr: PlanResultResponse => QueryPlanService.handlePlanResultResponse(prr)
    case per: PlanErrorResponse => QueryPlanService.handlePlanErrorResponse(per)

    case bqs: BatchQueryStatus => ModelResultsService.handleBatchQueryStatus(bqs)

    case qsr: QuerySaveResponse => ModelResultsService.handleQuerySaveResponse(qsr.savedQuery, qsr.error)
    case qdr: QueryDeleteResponse => ModelResultsService.handleQueryDeleteResponse(qdr.id, qdr.error)

    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.warn(s"Received unknown message of type [${rm.getClass.getSimpleName}].")
  }

  private[this] def handleQueryResultResponse(qrr: QueryResultResponse) = {
    if (qrr.results.size == 1 && qrr.results.headOption.exists(_.source.exists(_.dataOffset > 0))) {
      val result = qrr.results.headOption.getOrElse(throw new IllegalStateException())
      QueryAppendService.handleAppendQueryResult(qrr.id, result, qrr.durationMs)
    } else {
      QueryResultService.handleNewQueryResults(qrr.id, qrr.results, qrr.durationMs)
    }
  }
}
