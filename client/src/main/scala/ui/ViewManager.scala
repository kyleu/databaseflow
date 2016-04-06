package ui

import java.util.UUID

import models.{ GetTableRowData, GetViewDetail }
import models.schema.Table
import models.template.{ Icons, QueryEditorTemplate, TableColumnDetailTemplate, TableDefinitionTemplate }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NotificationService

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
      val queryId = UUID.randomUUID
      val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException("No Engine"))
      WorkspaceManager.append(QueryEditorTemplate.forView(engine, queryId, name, None, s"select * from $name").toString)

      views.get(name) match {
        case Some(view) => setViewDetails(queryId, view)
        case None => utils.NetworkMessage.sendMessage(GetViewDetail(name))
      }

      TabManager.addTab(queryId, "view-" + name, name, Icons.view)

      val queryPanel = $(s"#panel-$queryId")
      def crash() = NotificationService.info("Table Not Loaded", "Please retry in a moment.")

      $(".columns-link", queryPanel).click({ (e: JQueryEventObject) =>
        views.get(name) match {
          case Some(view) => viewColumns(queryId, view)
          case None => crash()
        }
        false
      })

      $(".definition-link", queryPanel).click({ (e: JQueryEventObject) =>
        views.get(name) match {
          case Some(view) => viewDefinition(queryId, view)
          case None => crash()
        }
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
    if (view.definition.isDefined) {
      $(".definition-link", panel).removeClass("initially-hidden")
    }

    utils.Logging.debug(s"View [${view.name}] loaded.")
  }

  private[this] def viewColumns(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableColumnDetailTemplate.columnsForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }

  private[this] def viewDefinition(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableDefinitionTemplate.definitionForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }
}
