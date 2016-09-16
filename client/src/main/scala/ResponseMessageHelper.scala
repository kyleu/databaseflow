import models._
import services._
import ui.modal.DataInsertManager
import ui.{HistoryManager, UserManager}
import utils.{Logging, NetworkMessage}

trait ResponseMessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {

    case p: Pong => NetworkMessage.latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)

    case us: UserSettings => UserManager.onUserSettings(us)

    case sqr: SavedQueryResponse => ModelResultsService.handleSavedQueryResponse(sqr)
    case srr: SharedResultResponse => ModelResultsService.handleSharedResultResponse(srr)

    case sr: SchemaResponse => ModelResultsService.handleSchemaResponse(sr)
    case tr: TableResponse => ModelResultsService.handleTableResponse(tr.tables)
    case vr: ViewResponse => ModelResultsService.handleViewResponse(vr.views)
    case pr: ProcedureResponse => ModelResultsService.handleProcedureResponse(pr.procedures)

    case arr: AuditRecordResponse => HistoryManager.handleAuditHistoryResponse(arr.history)
    case arr: AuditRecordRemoved => HistoryManager.handleAuditHistoryRemoved(arr.id)

    case ts: TransactionStatus => TransactionService.handleTransactionStatus(ts.state, ts.occurred)

    case qcr: QueryCheckResponse => QueryErrorService.handleQueryCheckResponse(qcr)
    case qcr: QueryCancelledResponse => NotificationService.info("Query Cancelled", "Active query has been cancelled.")
    case qrr: QueryResultResponse => if (qrr.result.source.exists(_.dataOffset > 0)) {
      QueryAppendService.handleAppendQueryResult(qrr.id, qrr.result)
    } else if (qrr.result.isStatement) {
      StatementResultService.handleNewStatementResults(qrr.id, qrr.index, qrr.result, qrr.durationMs)
    } else {
      QueryResultService.handleNewQueryResults(qrr.id, qrr.index, qrr.result, qrr.durationMs)
    }
    case qrrc: QueryResultRowCount => RowCountService.handleResultRowCount(qrrc)
    case qer: QueryErrorResponse => QueryErrorService.handleQueryErrorResponse(qer)

    case cdr: ChartDataResponse => ChartService.handleChartDataResponse(cdr)

    case prr: PlanResultResponse => QueryPlanService.handlePlanResultResponse(prr)
    case per: PlanErrorResponse => QueryPlanService.handlePlanErrorResponse(per)

    case bqs: BatchQueryStatus => ModelResultsService.handleBatchQueryStatus(bqs)

    case qsr: QuerySaveResponse => ModelResultsService.handleQuerySaveResponse(qsr.savedQuery, qsr.error)
    case qdr: QueryDeleteResponse => ModelResultsService.handleQueryDeleteResponse(qdr.id, qdr.error)

    case srsr: SharedResultSaveResponse => ModelResultsService.handleSharedResultSaveResponse(srsr.result, srsr.error)

    case irr: InsertRowResponse => DataInsertManager.handleInsertRowResponse(irr.resultId, irr.errors)

    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.warn(s"Received unknown message of type [${rm.getClass.getSimpleName}].")
  }
}
