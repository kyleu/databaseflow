package ui

import java.util.UUID

import models.engine.DatabaseEngine
import models.query.SavedQuery
import models.schema.Schema
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

object MetadataManager {
  var engine: Option[DatabaseEngine] = None
  var schema: Option[Schema] = None

  var savedQueries: Option[Seq[(String, JQuery, JQuery)]] = None

  def updateSavedQueries(updates: Seq[SavedQuery]) = {
    val updatedIds = updates.map(_.id)
    val sqs = (SavedQueryManager.savedQueries.filterNot(sq => updatedIds.contains(sq._1)).values ++ updates).toSeq.sortBy(_.name)

    SavedQueryManager.savedQueries = sqs.map(s => s.id -> s).toMap

    if (sqs.nonEmpty) {
      $("#saved-query-list-toggle").css("display", "block")
      val savedQueryList = $("#saved-query-list")
      savedQueryList.html(SidenavTemplate.savedQueries(sqs).mkString("\n"))
      $(".sidenav-link", savedQueryList).click { (e: JQueryEventObject) =>
        val id = UUID.fromString($(e.delegateTarget).data("key").toString)
        SavedQueryManager.savedQueryDetail(id)
        false
      }
    } else {
      $("#saved-query-list-toggle").css("display", "none")
    }

    savedQueries = Some(sqs.map { x =>
      val el = $("#saved-query-link-" + x.id)
      (x.id.toString, el, $("span", el))
    })
    ModelListManager.updatePanel("saved-query")
  }

  def updateSchema(sch: Schema) = {
    MetadataUpdates.updateTables(sch.tables)
    MetadataUpdates.updateViews(sch.views)
    MetadataUpdates.updateProcedures(sch.procedures)

    schema = Some(sch)
    engine = Some(DatabaseEngine.get(sch.engine))
  }

  def getSavedQuery(id: String) = savedQueries.flatMap(_.find(_._1 == id)).map(_._1)
}
