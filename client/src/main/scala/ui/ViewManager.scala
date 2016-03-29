package ui

import java.util.UUID

import models.schema.Table
import models.template.{ Icons, ViewDetailTemplate }
import models.{ RequestMessage, GetTableRowData }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object ViewManager {
  var views = Map.empty[String, Table]
  var openViews = Map.empty[String, UUID]

  def viewDetail(name: String, sendMessage: (RequestMessage) => Unit) = openViews.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(ViewDetailTemplate.forView(queryId, name).toString)

      TabManager.addTab(queryId, name, Icons.view)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, name, sendMessage)
        false
      })

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openViews = openViews - name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openViews = openViews + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, view: String, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ShowViewData(queryId = queryId, name = view))
  }
}
