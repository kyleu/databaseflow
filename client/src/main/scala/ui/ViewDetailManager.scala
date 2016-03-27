package ui

import java.util.UUID

import models.schema.Table
import models.template.{ Icons, ViewDetailTemplate }
import models.{ RequestMessage, ShowTableData }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NotificationService

object ViewDetailManager {
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
        views.get(name) match {
          case Some(view) => viewData(queryId, view, sendMessage)
          case None => NotificationService.info("View Not Loaded", "Please retry in a moment.")
        }
        false
      })

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openViews = openViews - name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openViews = openViews + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, view: Table, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ShowTableData(queryId = queryId, name = view.name))
  }
}
