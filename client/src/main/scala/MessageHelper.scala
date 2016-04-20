import models._
import utils.Logging

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)

    case srr: StatementResultResponse => handleStatementResultResponse(srr)
    case qrr: QueryResultResponse => handleQueryResultResponse(qrr)
    case qer: QueryErrorResponse => handleQueryErrorResponse(qer)

    case bqs: BatchQueryStatus => handleBatchQueryStatus(bqs)

    case sqrr: SavedQueryResultResponse => handleSavedQueryResponse(sqrr)

    case srr: SchemaResultResponse => handleSchemaResultResponse(srr)
    case trr: TableResultResponse => handleTableResultResponse(trr.tables)
    case vrr: ViewResultResponse => handleViewResultResponse(vrr.views)
    case prr: ProcedureResultResponse => handleProcedureResultResponse(prr.procedures)

    case prr: PlanResultResponse => handlePlanResultResponse(prr)
    case per: PlanErrorResponse => handlePlanErrorResponse(per)

    case qsr: QuerySaveResponse => handleQuerySaveResponse(qsr.savedQuery, qsr.error)
    case qdr: QueryDeleteResponse => handleQueryDeleteResponse(qdr.id, qdr.error)

    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.warn(s"Received unknown message of type [${rm.getClass.getSimpleName}].")
  }
}
