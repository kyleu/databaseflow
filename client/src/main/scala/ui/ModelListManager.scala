package ui

import java.util.UUID

import models.template.Icons
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object ModelListManager {
  var openLists = Map.empty[String, UUID]

  def showList(key: String) = openLists.get(key) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID

      //WorkspaceManager.append(ProcedureDetailTemplate.forProcedure(queryId, name).toString)
      //TabManager.addTab(queryId, name, Icons.procedure)

      val queryPanel = $(s"#panel-$queryId")

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openLists = openLists - key
        closeList(queryId)
        false
      })

      openLists = openLists + (key -> queryId)
  }

  private def closeList(queryId: UUID): Unit = {
    if (QueryManager.activeQueries.isEmpty) {
      AdHocQueryManager.addNewQuery()
    }

    $(s"#panel-$queryId").remove()
    TabManager.removeTab(queryId)

    TabManager.selectTab(QueryManager.activeQueries.head)
  }
}
