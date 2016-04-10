package ui

import java.util.UUID

import models.engine.EngineQueries
import models.query.RowDataOptions
import models.schema.View
import models.template._
import models.{ GetViewDetail, GetViewRowData, SubmitQuery }
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }
import services.NotificationService

object ViewManager {
  var openViews = Map.empty[String, UUID]

  def addView(view: View) = {
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
      WorkspaceManager.append(ViewDetailTemplate.forView(engine, queryId, name).toString)

      MetadataManager.schema.flatMap(_.views.find(_.name == name)) match {
        case Some(view) => setViewDetails(queryId, view)
        case None => utils.NetworkMessage.sendMessage(GetViewDetail(name))
      }

      TabManager.addTab(queryId, "view-" + name, name, Icons.view)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, name, RowDataOptions.empty)
        false
      })

      def wire(q: JQuery, action: String) = q.click({ (e: JQueryEventObject) =>
        val sql = EngineQueries.selectFrom(name, limit = Some(5))(MetadataManager.engine.getOrElse(throw new IllegalStateException("No engine.")))
        utils.NetworkMessage.sendMessage(SubmitQuery(queryId, sql, Some(action)))
        false
      })

      wire($(".explain-view-link", queryPanel), "explain")
      wire($(".analyze-view-link", queryPanel), "analyze")

      def crash() = NotificationService.info("Table Not Loaded", "Please retry in a moment.")

      $(".columns-link", queryPanel).click({ (e: JQueryEventObject) =>
        MetadataManager.schema.flatMap(_.views.find(_.name == name)) match {
          case Some(view) => viewColumns(queryId, view)
          case None => crash()
        }
        false
      })

      $(".definition-link", queryPanel).click({ (e: JQueryEventObject) =>
        MetadataManager.schema.flatMap(_.views.find(_.name == name)) match {
          case Some(view) => viewDefinition(queryId, view)
          case None => crash()
        }
        false
      })

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openViews = openViews - name
        QueryManager.closeQuery(queryId)
        false
      })

      openViews = openViews + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, name: String, options: RowDataOptions) = {
    utils.NetworkMessage.sendMessage(GetViewRowData(queryId = queryId, name = name, options = options))
  }

  private[this] def setViewDetails(uuid: UUID, view: View) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing view panel for [$uuid].")
    }

    view.description.map { desc =>
      $(".description", panel).text(desc)
    }

    val summary = s"View contains ${view.columns.size} columns."
    $(".summary", panel).text(summary)

    if (view.columns.nonEmpty) {
      $(".columns-link", panel).removeClass("initially-hidden")
    }
    if (view.definition.isDefined) {
      $(".definition-link", panel).removeClass("initially-hidden")
    }

    utils.Logging.debug(s"View [${view.name}] loaded.")
  }

  private[this] def viewColumns(queryId: UUID, view: View) = {
    val id = UUID.randomUUID
    val html = ViewColumnDetailTemplate.columnsForView(id, queryId, view)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }

  private[this] def viewDefinition(queryId: UUID, view: View) = {
    val id = UUID.randomUUID
    val html = ViewDefinitionTemplate.definitionForView(id, queryId, view)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .${Icons.close}").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }
}
