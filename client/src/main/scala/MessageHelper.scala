import models._
import utils.Logging

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)

    case qr: QueryResultResponse => handleQueryResultResponse(qr)
    case qe: QueryErrorResponse => handleQueryErrorResponse(qe)

    case sqrr: SavedQueryResultResponse => handleSavedQueryResponse(sqrr)
    case trr: TableResultResponse => handleTableResultResponse(trr)
    case vrr: ViewResultResponse => handleViewResultResponse(vrr)
    case prr: ProcedureResultResponse => handleProcedureResultResponse(prr)

    case pr: PlanResultResponse => handlePlanResultResponse(pr)

    case qsr: QuerySaveResponse => handleQuerySaveResponse(qsr.savedQuery, qsr.error)
    case qdr: QueryDeleteResponse => handleQueryDeleteResponse(qdr.id, qdr.error)

    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.warn(s"Received unknown message of type [${rm.getClass.getSimpleName}].")
  }
}
