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

      val name = key match {
        case "saved-query" => "Saved Queries"
        case "table" => "Tables"
        case "view" => "Views"
        case "procedure" => "Stored Procedures"
        case _ => throw new IllegalArgumentException(s"Invalid key [$key].")
      }

      val html = ModelListTemplate.forModels(queryId, key, name).toString

      WorkspaceManager.append(html)
      TabManager.addTab(queryId, name, Icons.list)
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
