package ui

import java.util.UUID

import models.engine.EngineQueries
import models.query.RowDataOptions
import models.schema.View
import models.template._
import models.{ GetViewDetail, GetViewRowData, SubmitQuery }
import org.scalajs.jquery.{ JQuery, jQuery => $ }

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
        case Some(view) if view.columns.nonEmpty => setViewDetails(queryId, view)
        case _ => utils.NetworkMessage.sendMessage(GetViewDetail(name))
      }

      TabManager.addTab(queryId, "view-" + name, name, Icons.view)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      utils.JQueryUtils.clickHandler($(".view-data-link", queryPanel), (jq) => {
        viewData(queryId, name, RowDataOptions.empty)
      })

      def wire(q: JQuery, action: String) = utils.JQueryUtils.clickHandler(q, (jq) => {
        // TODO build card to hold result
        val sql = EngineQueries.selectFrom(name, RowDataOptions.empty)(MetadataManager.engine.getOrElse(throw new IllegalStateException("No engine.")))
        utils.NetworkMessage.sendMessage(SubmitQuery(queryId = queryId, sql = sql, action = Some(action)))
      })

      wire($(".explain-view-link", queryPanel), "explain")
      wire($(".analyze-view-link", queryPanel), "analyze")

      utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
        openViews = openViews - name
        QueryManager.closeQuery(queryId)
      })

      openViews = openViews + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, name: String, options: RowDataOptions) = {
    // TODO build card to hold result
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
      import scalatags.Text.all._
      val section = $(".definition-section", panel)
      section.removeClass("initially-hidden")
      $(".section-content", section).html(pre(cls := "pre-wrap")(definition).render)
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
