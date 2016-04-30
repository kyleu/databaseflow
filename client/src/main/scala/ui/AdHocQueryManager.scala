package ui

import java.util.UUID

import models.engine.EngineQueries
import models.query.{ RowDataOptions, SavedQuery }
import models.template.{ Icons, QueryEditorTemplate }
import org.scalajs.jquery.{ jQuery => $ }

import scala.util.Random

object AdHocQueryManager {
  private[this] var lastNum = 1

  def addNewQuery(queryId: UUID = UUID.randomUUID) = {
    val queryName = if (lastNum == 1) {
      "Untitled Query"
    } else {
      "Untitled Query " + lastNum
    }
    val sql = MetadataManager.schema.map { s =>
      if (s.tables.isEmpty) {
        ""
      } else {
        val engine = MetadataManager.engine.getOrElse(throw new IllegalStateException())
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
    TabManager.addTab(queryId, "adhoc-" + queryId, queryName, Icons.adHocQuery)

    val queryPanel = $(s"#panel-$queryId")

    utils.JQueryUtils.clickHandler($(".save-query-link", queryPanel), (jq) => {
      QuerySaveFormManager.show(SavedQuery(
        id = queryId,
        name = queryName,
        sql = QueryManager.getSql(queryId)
      ))
    })

    utils.JQueryUtils.clickHandler($(".export-link", queryPanel), (jq) => {
      QueryExportFormManager.show(queryId, QueryManager.getSql(queryId))
    })

    def onChange(s: String): Unit = {
      if (s == sql) {
        $(".unsaved-status", queryPanel).css("display", "none")
      } else {
        $(".unsaved-status", queryPanel).css("display", "inline")
      }
    }

    QueryManager.addQuery(queryId, "Untitled Query", queryPanel, onChange, () => Unit)
  }

}
