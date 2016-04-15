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

    view.definition.map { definition =>
      val section = $(".definition-section", panel)
      section.removeClass("initially-hidden")
      $(".section-content", section).html(ViewDefinitionTemplate.definitionPanel(definition).render)
    }
    if (view.columns.nonEmpty) {
      val section = $(".columns-section", panel)
      section.removeClass("initially-hidden")
      $(".badge", section).html(view.columns.size.toString)
      $(".section-content", section).html(ViewColumnDetailTemplate.columnPanel(view.columns).render)
    }

    scalajs.js.Dynamic.global.$(".collapsible", panel).collapsible()

    utils.Logging.debug(s"View [${view.name}] loaded.")
  }
}
