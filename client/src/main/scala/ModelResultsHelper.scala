import java.util.UUID

import models._
import models.query.SavedQuery
import services.NotificationService
import ui._

trait ModelResultsHelper { this: DatabaseFlow =>
  protected[this] def handleBatchQueryStatus(bqs: BatchQueryStatus) = {
    SampleDatabaseManager.process(bqs)
  }

  protected[this] def handleSavedQueryResponse(sqrr: SavedQueryResultResponse) = {
    SavedQueryManager.updateSavedQueries(sqrr.savedQueries)
  }
  protected[this] def handleTableResultResponse(tr: TableResultResponse) = {
    MetadataUpdates.updateTables(tr.tables)
    tr.tables.foreach(TableManager.addTable)
  }
  protected[this] def handleViewResultResponse(vrr: ViewResultResponse) = {
    MetadataUpdates.updateViews(vrr.views)
    vrr.views.foreach(ViewManager.addView)
  }
  protected[this] def handleProcedureResultResponse(prr: ProcedureResultResponse) = {
    MetadataUpdates.updateProcedures(prr.procedures)
    prr.procedures.foreach(ProcedureManager.addProcedure)
  }

  protected[this] def handleQuerySaveResponse(sq: SavedQuery, error: Option[String]) = error match {
    case Some(err) => NotificationService.info("Query Save Error", err)
    case None => QueryFormManager.handleQuerySaveResponse(sq, error)
  }

  protected[this] def handleQueryDeleteResponse(id: UUID, error: Option[String]) = error match {
    case Some(err) => NotificationService.info("Query Delete Error", err)
    case None => SavedQueryManager.deleteQuery(id)
  }
}
