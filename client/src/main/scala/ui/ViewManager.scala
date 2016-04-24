package ui

import java.util.UUID

import models.engine.EngineQueries
import models.query.RowDataOptions
import models.schema.View
import models.template._
import models.{ GetViewDetail, GetViewRowData, SubmitQuery }
import org.scalajs.jquery.{ JQuery, jQuery => $ }
import utils.JQueryUtils

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
        viewData(queryId, name, RowDataOptions.empty)
      })

      def wire(q: JQuery, action: String) = utils.JQueryUtils.clickHandler(q, (jq) => {
        val resultId = UUID.randomUUID
        val title = "Query Plan"
        ProgressManager.startProgress(queryId, resultId, () => Unit, Icons.loading, title)

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

  private[this] def viewData(queryId: UUID, name: String, options: RowDataOptions): Unit = {
    val resultId = UUID.randomUUID

    def onComplete(): Unit = {
      val panel = $(s"#$resultId")
      if (panel.length != 1) {
        throw new IllegalStateException(s"Found [${panel.length}] panels for result [$resultId].")
      }
      JQueryUtils.clickHandler($(".sorted-title", panel), (j) => {
        val col = j.data("col").toString
        val asc = j.data("dir").toString == "asc"

        viewData(queryId, name, options.copy(
          orderByCol = Some(col),
          orderByAsc = Some(!asc)
        ))
      })
    }

    ProgressManager.startProgress(queryId, resultId, onComplete, Icons.loading, name)

    utils.NetworkMessage.sendMessage(GetViewRowData(queryId = queryId, name = name, options = options, resultId = resultId))
  }
}
