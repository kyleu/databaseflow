package ui

import java.util.UUID

import models.engine.EngineQueries
import models.query.RowDataOptions
import models.schema.View
import models.template._
import models.{ GetViewDetail, SubmitQuery }
import org.scalajs.jquery.{ JQuery, jQuery => $ }

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
        RowDataManager.showViewRowData(queryId, name, RowDataOptions(limit = Some(UserManager.rowsReturned)))
      })

      utils.JQueryUtils.clickHandler($(".export-link", queryPanel), (jq) => {
        implicit val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException("Schema not initialized"))
        QueryExportFormManager.show(queryId, EngineQueries.selectFrom(name), name)
      })

      def wire(q: JQuery, action: String) = utils.JQueryUtils.clickHandler(q, (jq) => {
        val resultId = UUID.randomUUID
        val title = "Query Plan"
        ProgressManager.startProgress(queryId, resultId, () => Unit, title)

        val sql = EngineQueries.selectFrom(name, RowDataOptions.empty)(MetadataManager.engine.getOrElse(throw new IllegalStateException("No engine.")))
        utils.NetworkMessage.sendMessage(SubmitQuery(queryId = queryId, sql = sql, action = Some(action), resultId = resultId))
      })

      wire($(".explain-view-link", queryPanel), "explain")
      wire($(".analyze-view-link", queryPanel), "analyze")

      utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
        openViews = openViews - name
        QueryManager.closeQuery(queryId)
      })

      openViews = openViews + (name -> queryId)
  }
}
