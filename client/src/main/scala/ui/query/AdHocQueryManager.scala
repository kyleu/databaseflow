package ui.query

import java.util.UUID

import models.CloseQuery
import models.engine.EngineQueries
import models.query.{RowDataOptions, SavedQuery}
import models.template.Icons
import models.template.query.QueryEditorTemplate
import org.scalajs.jquery.{jQuery => $}
import services.TextChangeService
import ui.metadata.MetadataManager
import ui.modal.SavedQueryFormManager
import ui.UserManager
import ui.tabs.TabManager
import util.{Messages, NetworkMessage, TemplateHelper}

import scala.util.Random

object AdHocQueryManager {
  private[this] var lastNum = 1

  def addNewQuery(queryId: UUID = UUID.randomUUID, initialSql: Option[String] = None) = {
    val queryName = if (lastNum == 1) {
      Messages("query.default.name", "").trim
    } else {
      Messages("query.default.name", lastNum)
    }
    val sql = initialSql match {
      case Some(s) => s
      case None => MetadataManager.schema.map { s =>
        if (s.tables.isEmpty) {
          ""
        } else {
          val t = s.tables(Random.nextInt(s.tables.size)).name
          EngineQueries.selectFrom(t, Nil, RowDataOptions.empty)(MetadataManager.getEngine)._1
        }
      }.getOrElse("")
    }
    addAdHocQuery(queryId, queryName, sql)
    lastNum += 1
  }

  def addAdHocQuery(queryId: UUID, queryName: String, sql: String): Unit = {
    val html = QueryEditorTemplate.forAdHocQuery(MetadataManager.getEngine, queryId, queryName, sql)
    QueryManager.workspace.append(html.render)

    def close() = if (QueryManager.activeQueries.contains(queryId)) {
      if (QueryManager.closeQuery(queryId)) {
        NetworkMessage.sendMessage(CloseQuery(queryId))
      }
    }

    TabManager.addTab(queryId, "adhoc-" + queryId, queryName, Icons.adHocQuery, close _)

    val queryPanel = $(s"#panel-$queryId")

    TemplateHelper.clickHandler($(".save-query-link", queryPanel), _ => {
      val owner = UserManager.userId.getOrElse(throw new IllegalStateException())
      val params = ParameterManager.getParamsOpt(queryId).getOrElse(Seq.empty)
      val newSql = SqlManager.getSql(queryId)
      SavedQueryFormManager.show(SavedQuery(queryId, queryName, sql = newSql, params = params, owner = owner))
    })

    val runQueryLink = $(".run-query-link", queryPanel)
    val runQueryAllLink = $(".run-query-all-link", queryPanel)

    def onChange(s: String): Unit = {
      if (s == sql) {
        TextChangeService.markClean(queryId)
        $(".unsaved-status", queryPanel).css("display", "none")
      } else {
        TextChangeService.markDirty(queryId)
        $(".unsaved-status", queryPanel).css("display", "inline")
      }
      SqlManager.updateLinks(queryId, runQueryLink, runQueryAllLink)
    }

    QueryManager.addQuery(queryId, "Untitled Query", queryPanel, sql, Seq.empty, onChange)
  }
}
