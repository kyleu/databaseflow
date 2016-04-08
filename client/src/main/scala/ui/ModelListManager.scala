package ui

import java.util.UUID

import models.template.{ Icons, ModelListTemplate }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object ModelListManager {
  var openLists = Map.empty[String, UUID]

  def showList(key: String) = openLists.get(key) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID

      val schema = MetadataManager.schema.getOrElse(throw new IllegalStateException("Schema not available."))

      val template = key match {
        case "saved-query" => ModelListTemplate.forSavedQueries(queryId, SavedQueryManager.savedQueries.values.toSeq.sortBy(_.name))
        case "table" => ModelListTemplate.forTables(queryId, schema.tables.sortBy(_.name))
        case "view" => ModelListTemplate.forViews(queryId, schema.views.sortBy(_.name))
        case "procedure" => ModelListTemplate.forProcedures(queryId, schema.procedures.sortBy(_.name))
        case _ => throw new IllegalArgumentException(s"Invalid key [$key].")
      }

      WorkspaceManager.append(template._2.toString)
      TabManager.addTab(queryId, "list-" + key, template._1, Icons.list)
      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      val queryPanel = $(s"#panel-$queryId")

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openLists = openLists - key
        closeList(queryId)
        false
      })

      openLists = openLists + (key -> queryId)
  }

  private def closeList(queryId: UUID): Unit = {
    QueryManager.closeQuery(queryId)
  }
}
