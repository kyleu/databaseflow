package services

import java.util.UUID

import models.query.SavedQuery
import models.schema.{ Procedure, Table, View }
import models.{ BatchQueryStatus, SavedQueryResultResponse, SchemaResultResponse }
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ ProcedureManager, QuerySaveFormManager, TableManager, ViewManager, _ }

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
    if (MetadataManager.schema.isEmpty) {
      MetadataManager.updateSchema(srr.schema)
      $("#loading-panel").hide()
      if (!receivedSchemaResultResponse) {
        receivedSchemaResultResponse = true
        if (receivedSavedQueryResponse) {
          InitService.performInitialAction()
        }
      }
    } else {
      MetadataManager.updateSchema(srr.schema)
    }
  }

  def handleTableResultResponse(t: Seq[Table]) = {
    MetadataUpdates.updateTables(t)
    t.foreach(TableManager.addTable)
  }
  def handleViewResultResponse(v: Seq[View]) = {
    MetadataUpdates.updateViews(v)
    v.foreach(ViewManager.addView)
  }
  def handleProcedureResultResponse(p: Seq[Procedure]) = {
    MetadataUpdates.updateProcedures(p)
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
