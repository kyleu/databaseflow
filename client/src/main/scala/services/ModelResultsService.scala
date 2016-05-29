package services

import java.util.UUID

import models.query.SavedQuery
import models.schema.{ Procedure, Table, View }
import models.{ BatchQueryStatus, SavedQueryResultResponse, SchemaResultResponse }
import org.scalajs.jquery.{ jQuery => $ }
import ui.metadata._
import ui.modal.QuerySaveFormManager
import ui.query.{ ProcedureManager, SavedQueryManager, TableManager, ViewManager }
import ui.ProcedureManager

object ModelResultsService {
  private[this] var receivedSavedQueryResponse = false
  private[this] var receivedSchemaResultResponse = false

  def handleBatchQueryStatus(bqs: BatchQueryStatus) = {
    SampleDatabaseManager.process(bqs)
  }

  def handleSavedQueryResponse(sqrr: SavedQueryResultResponse) = {
    SavedQueryManager.updateSavedQueries(sqrr.savedQueries)
    if (!receivedSavedQueryResponse) {
      receivedSavedQueryResponse = true
      if (receivedSchemaResultResponse) {
        InitService.performInitialAction()
      }
    }
  }

  def handleSchemaResultResponse(srr: SchemaResultResponse) = {
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
        if (receivedSavedQueryResponse) {
          InitService.performInitialAction()
        }
      }
    }
  }

  def handleTableResultResponse(t: Seq[Table]) = {
    TableUpdates.updateTables(t, fullSchema = false)
    t.foreach(TableManager.addTable)
  }
  def handleViewResultResponse(v: Seq[View]) = {
    ViewUpdates.updateViews(v, fullSchema = false)
    v.foreach(ViewManager.addView)
  }
  def handleProcedureResultResponse(p: Seq[Procedure]) = {
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
