package ui.query

import java.util.UUID

import models.engine.EngineQueries
import models.query.RowDataOptions
import models.schema.View
import models.template._
import models.template.view.ViewDetailTemplate
import models.{GetViewDetail, SubmitQuery}
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.metadata.MetadataManager
import ui.tabs.TabManager
import ui.{ProgressManager, UserManager, _}
import utils.{NetworkMessage, TemplateUtils}

object ViewManager extends ViewDetailHelper {
  var openViews = Map.empty[String, UUID]

  def addView(view: View) = {
    openViews.get(view.name).foreach { uuid =>
      setViewDetails(uuid, view)
    }
  }

  def viewDetail(name: String) = openViews.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
      queryId
    case None =>
      val queryId = UUID.randomUUID
      val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException("No Engine"))
      WorkspaceManager.append(ViewDetailTemplate.forView(engine, queryId, name).toString)

      MetadataManager.schema.flatMap(_.views.find(_.name == name)) match {
        case Some(view) if view.columns.nonEmpty => setViewDetails(queryId, view)
        case _ => NetworkMessage.sendMessage(GetViewDetail(name))
      }

      def close() = if (QueryManager.closeQuery(queryId)) {
        openViews = openViews - name
      }

      TabManager.addTab(queryId, "view-" + name, name, Icons.view, close _)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      TemplateUtils.clickHandler($(".view-data-link", queryPanel), _ => {
        RowDataManager.showRowData("view", queryId, name, RowDataOptions(limit = Some(UserManager.rowsReturned)), UUID.randomUUID)
      })
      TemplateUtils.clickHandler($(".query-open-link", queryPanel), _ => {
        AdHocQueryManager.addAdHocQuery(UUID.randomUUID, "Query Name", "select * from something")
      })

      def wire(q: JQuery, action: String) = TemplateUtils.clickHandler(q, _ => {
        val resultId = UUID.randomUUID
        val title = "Query Plan"
        ProgressManager.startProgress(queryId, resultId, title)

        val sql = EngineQueries.selectFrom(name, RowDataOptions.empty)(MetadataManager.engine.getOrElse(throw new IllegalStateException("No engine.")))._1
        NetworkMessage.sendMessage(SubmitQuery(queryId = queryId, sql = sql, params = Seq.empty, action = Some(action), resultId = resultId))
      })

      wire($(".explain-view-link", queryPanel), "explain")
      wire($(".analyze-view-link", queryPanel), "analyze")

      openViews = openViews + (name -> queryId)
  }
}
