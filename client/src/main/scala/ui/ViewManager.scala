package ui

import java.util.UUID

import models.schema.Table
import models.template.{ Icons, ViewDetailTemplate }
import models.{ GetViewDetail, RequestMessage }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object ViewManager {
  var views = Map.empty[String, Table]
  var openViews = Map.empty[String, UUID]

  def addView(view: Table) = {
    views = views + (view.name -> view)
    openViews.get(view.name).foreach { uuid =>
      setViewDetails(uuid, view)
    }
  }

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

  private[this] def viewData(queryId: UUID, viewName: String, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(GetViewDetail(name = viewName))
  }

  private[this] def setViewDetails(uuid: UUID, view: Table) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing view panel for [$uuid].")
    }

    view.description.map { desc =>
      $(".description", panel).text(desc)
    }

    val summary = s"Table contains ${view.columns.size} columns, ${view.indexes.size} indexes, and ${view.foreignKeys.size} foreign keys."
    $(".summary", panel).text(summary)

    if (view.columns.nonEmpty) {
      $(".columns-link", panel).removeClass("initially-hidden")
    }
    if (view.indexes.nonEmpty) {
      $(".indexes-link", panel).removeClass("initially-hidden")
    }
    if (view.foreignKeys.nonEmpty) {
      $(".foreign-keys-link", panel).removeClass("initially-hidden")
    }

    utils.Logging.debug(s"View [${view.name}] loaded.")
  }
}