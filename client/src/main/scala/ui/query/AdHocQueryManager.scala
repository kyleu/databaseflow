package ui.query

import java.util.UUID

import models.engine.EngineQueries
import models.query.{RowDataOptions, SavedQuery}
import models.template.Icons
import models.template.query.QueryEditorTemplate
import org.scalajs.jquery.{jQuery => $}
import ui.{TabManager, UserManager}
import ui.metadata.MetadataManager
import ui.modal.{QueryExportFormManager, QuerySaveFormManager}
import utils.{Messages, TemplateUtils}

import scala.util.Random

object AdHocQueryManager {
  private[this] var lastNum = 1

  def addNewQuery(queryId: UUID = UUID.randomUUID) = {
    val queryName = if (lastNum == 1) {
      Messages("query.default.name", "").trim
    } else {
      Messages("query.default.name", lastNum)
    }
    val sql = MetadataManager.schema.map { s =>
      if (s.tables.isEmpty) {
        ""
      } else {
        val t = s.tables(Random.nextInt(s.tables.size)).name
        EngineQueries.selectFrom(t, RowDataOptions.empty)(MetadataManager.engine.getOrElse(throw new IllegalStateException("No engine.")))
      }
    }.getOrElse("")
    addAdHocQuery(queryId, queryName, sql)
    lastNum += 1
  }

  def addAdHocQuery(queryId: UUID, queryName: String, sql: String): Unit = {
    val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException("No Engine"))
    val html = QueryEditorTemplate.forAdHocQuery(engine, queryId, queryName, sql)
    QueryManager.workspace.append(html.render)

    def close() = {
      QueryManager.closeQuery(queryId)
    }

    TabManager.addTab(queryId, "adhoc-" + queryId, queryName, Icons.adHocQuery, close)

    val queryPanel = $(s"#panel-$queryId")

    TemplateUtils.clickHandler($(".save-query-link", queryPanel), (jq) => {
      val owner = UserManager.userId.getOrElse(throw new IllegalStateException())
      QuerySaveFormManager.show(SavedQuery(queryId, queryName, sql = SqlManager.getSql(queryId), owner = owner))
    })

    TemplateUtils.clickHandler($(".export-link", queryPanel), (jq) => {
      QueryExportFormManager.show(queryId, SqlManager.getSql(queryId), "Export")
    })

    val runQueryLink = $(".run-query-link", queryPanel)
    val runQueryAllLink = $(".run-query-all-link", queryPanel)

    def onChange(s: String): Unit = {
      if (s == sql) {
        $(".unsaved-status", queryPanel).css("display", "none")
      } else {
        $(".unsaved-status", queryPanel).css("display", "inline")
      }
      SqlManager.updateLinks(queryId, runQueryLink, runQueryAllLink)
    }

    QueryManager.addQuery(queryId, "Untitled Query", queryPanel, onChange)
  }
}
