package ui

import java.util.UUID

import models.query.SavedQuery
import models.{ GetTableRowData, GetViewDetail }
import models.schema.Table
import models.template.{ Icons, QueryEditorTemplate }
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

  def viewDetail(name: String) = openViews.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      if (!views.isDefinedAt(name)) {
        utils.NetworkMessage.sendMessage(GetViewDetail(name))
      }

      val queryId = UUID.randomUUID
      WorkspaceManager.append(QueryEditorTemplate.forView(queryId, name, None, s"select * from $name").toString)

      TabManager.addTab(queryId, name, Icons.view)

      val queryPanel = $(s"#panel-$queryId")

      $(s".save-query-link", queryPanel).click({ (e: JQueryEventObject) =>
        QueryFormManager.show(SavedQuery(
          id = queryId,
          name = s"Untitled $name Query",
          sql = QueryManager.getSql(queryId)
        ))
        false
      })

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openViews = openViews - name
        QueryManager.closeQuery(queryId)
        false
      })

      openViews = openViews + (name -> queryId)
      QueryManager.addQuery(queryId, queryPanel, (s) => Unit, () => Unit)
  }

  private[this] def viewData(queryId: UUID, viewName: String) = {
    utils.NetworkMessage.sendMessage(GetTableRowData(queryId = queryId, name = viewName))
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
