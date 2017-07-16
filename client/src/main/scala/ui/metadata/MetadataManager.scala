package ui.metadata

import java.util.UUID

import models.RefreshSchema
import models.engine.DatabaseEngine
import models.query.{SavedQuery, SharedResult}
import models.schema.Schema
import models.template.SidenavTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import services.NotificationService
import ui.query._
import util.{NetworkMessage, TemplateUtils}

object MetadataManager {
  var engine: Option[DatabaseEngine] = None
  var schema: Option[Schema] = None
  var pendingRefresh = false

  var savedQueries: Option[Seq[(String, JQuery, JQuery)]] = None

  def updateSavedQueries(updates: Seq[SavedQuery]) = {
    val updatedIds = updates.map(_.id)
    val sqs = (SavedQueryManager.savedQueries.filterNot(sq => updatedIds.contains(sq._1)).values ++ updates).toSeq.sortBy(_.name)

    SavedQueryManager.savedQueries = sqs.map(s => s.id -> s).toMap

    if (sqs.nonEmpty) {
      $("#saved-query-list-toggle").css("display", "block")
      val savedQueryList = $("#saved-query-list")
      savedQueryList.html(SidenavTemplate.savedQueries(sqs).mkString("\n"))
      TemplateUtils.clickHandler($(".sidenav-link", savedQueryList), jq => {
        val id = UUID.fromString(jq.data("key").toString)
        SavedQueryManager.savedQueryDetail(id)
      })
    } else {
      $("#saved-query-list-toggle").css("display", "none")
    }

    savedQueries = Some(sqs.map { x =>
      val el = $("#saved-query-link-" + x.id)
      (x.id.toString, el, $("span", el))
    })
    ModelListManager.updatePanel("saved-query")
  }

  def updateSharedResults(updates: Seq[SharedResult]) = {
    val updatedIds = updates.map(_.id)
    val srs = (SharedResultManager.sharedResults.values.toSeq.filterNot(sr => updatedIds.contains(sr.id)) ++ updates).sortBy(_.title)

    SharedResultManager.sharedResults = srs.map(r => r.id -> r).toMap

    if (srs.nonEmpty) {
      $("#shared-result-list-toggle").css("display", "block")
      val sharedResultList = $("#shared-result-list")
      sharedResultList.html(SidenavTemplate.sharedResults(srs).mkString("\n"))
      TemplateUtils.clickHandler($(".sidenav-link", sharedResultList), jq => {
        val id = UUID.fromString(jq.data("key").toString)
        SharedResultManager.sharedResultDetail(id)
      })
    } else {
      $("#shared-result-list-toggle").css("display", "none")
    }

    ModelListManager.updatePanel("shared-result")
  }

  def refreshSchema() = {
    pendingRefresh = true
    NetworkMessage.sendMessage(RefreshSchema)
  }

  def updateSchema(sch: Schema, fullSchema: Boolean) = {
    TableUpdates.updateTables(sch.tables, fullSchema)
    sch.tables.foreach(TableManager.addTable)

    ViewUpdates.updateViews(sch.views, fullSchema)
    sch.views.foreach(ViewManager.addView)

    ProcedureUpdates.updateProcedures(sch.procedures, fullSchema)
    sch.procedures.foreach(ProcedureManager.addProcedure)

    schema = Some(sch)
    engine = Some(DatabaseEngine.withName(sch.engine))

    if (sch.tables.isEmpty) {
      NotificationService.error("No Content", "There are no tables or views.")
    }
  }

  def getSavedQuery(id: String) = savedQueries.flatMap(_.find(_._1 == id)).map(_._1)
}
