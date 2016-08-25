package services

import java.util.UUID

import models.query.SavedQuery
import models.schema.{Procedure, Table, View}
import models.{BatchQueryStatus, SavedQueryResponse, SchemaResponse, SharedResultResponse}
import org.scalajs.jquery.{jQuery => $}
import ui.metadata._
import ui.modal.QuerySaveFormManager
import ui.query._

object ModelResultsService {
  private[this] var receivedSavedQueryResponse = false
  private[this] var receivedSharedResultResponse = false
  private[this] var receivedSchemaResultResponse = false

  def startIfReady = if (receivedSavedQueryResponse && receivedSharedResultResponse && receivedSchemaResultResponse) {
    InitService.performInitialAction()
  }

  def handleBatchQueryStatus(bqs: BatchQueryStatus) = {
    SampleDatabaseManager.process(bqs)
  }

  def handleSavedQueryResponse(sqr: SavedQueryResponse) = {
    SavedQueryManager.updateSavedQueries(sqr.savedQueries, sqr.usernames)
    if (!receivedSavedQueryResponse) {
      receivedSavedQueryResponse = true
      startIfReady
    }
  }

  def handleSharedResultResponse(srr: SharedResultResponse) = {
    SharedResultManager.updateSharedResults(srr.sharedResults, srr.usernames)
    if (!receivedSharedResultResponse) {
      receivedSharedResultResponse = true
      startIfReady
    }
  }

  def handleSchemaResponse(srr: SchemaResponse) = {
    if (MetadataManager.pendingRefresh) {
      MetadataManager.pendingRefresh = false
      NotificationService.info("Schema Refresh", "Completed successfully.")
    }

    val newSchema = MetadataManager.schema.isEmpty
    MetadataManager.updateSchema(srr.schema, fullSchema = true)

    if (newSchema) {
      $("#loading-panel").hide()
      if (!receivedSchemaResultResponse) {
        receivedSchemaResultResponse = true
        startIfReady
      }
    }
  }

  def handleTableResponse(t: Seq[Table]) = {
    TableUpdates.updateTables(t, fullSchema = false)
    t.foreach(TableManager.addTable)
  }
  def handleViewResponse(v: Seq[View]) = {
    ViewUpdates.updateViews(v, fullSchema = false)
    v.foreach(ViewManager.addView)
  }
  def handleProcedureResponse(p: Seq[Procedure]) = {
    ProcedureUpdates.updateProcedures(p, fullSchema = false)
    p.foreach(ProcedureManager.addProcedure)
  }

  def handleQuerySaveResponse(sq: SavedQuery, error: Option[String]) = error match {
    case Some(err) => NotificationService.info("Query Save Error", err)
    case None => QuerySaveFormManager.handleQuerySaveResponse(sq, error)
  }

  def handleQueryDeleteResponse(id: UUID, error: Option[String]) = error match {
    case Some(err) => NotificationService.info("Query Delete Error", err)
    case None => SavedQueryManager.deleteQuery(id)
  }
}
