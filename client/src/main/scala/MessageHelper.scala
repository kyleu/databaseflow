import models._
import org.scalajs.jquery.{ jQuery => $ }
import utils.Logging

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)

    case qr: QueryResultResponse => handleQueryResultResponse(qr)
    case qe: QueryErrorResponse => handleQueryErrorResponse(qe)

    case tr: TableResultResponse => handleTableResultResponse(tr)
    case vrr: ViewResultResponse => handleViewResultResponse(vrr)

    case pr: PlanResultResponse => handlePlanResultResponse(pr)

    case qsr: QuerySaveResponse => handleQuerySaveResponse(qsr.savedQuery, qsr.error)

    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.warn(s"Received unknown message of type [${rm.getClass.getSimpleName}].")
  }
}
