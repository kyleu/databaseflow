package ui

import java.util.UUID

import models.schema.Table
import models.template.ViewDetailTemplate
import models.{ RequestMessage, ShowTable }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object ViewDetailManager {
  var openViews = Map.empty[String, UUID]

  def viewDetail(view: Table, sendMessage: (RequestMessage) => Unit) = openViews.get(view.name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(ViewDetailTemplate.forView(queryId, view).toString)

      TabManager.addTab(queryId, view.name, "bar-chart")

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, view, sendMessage)
        false
      })

      $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
        openViews = openViews - view.name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openViews = openViews + (view.name -> queryId)
  }

  private[this] def viewData(queryId: UUID, view: Table, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ShowTable(queryId = queryId, name = view.name))
  }
}
