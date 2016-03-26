package ui

import java.util.UUID

import models.{ RequestMessage, SubmitQuery }
import models.schema.Schema
import models.template.SqlEditorTemplate
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

import scala.scalajs.js
import scala.util.Random

object QueryManager {
  var activeQueries = Seq.empty[UUID]
  private[this] var lastNum = 1
  private[this] lazy val workspace = $("#workspace")

  def addNewQuery(sendMessage: (RequestMessage) => Unit): Unit = {
    TabManager.initIfNeeded()

    val queryId = UUID.randomUUID
    val queryName = if (lastNum == 1) {
      "Untitled Query"
    } else {
      "Untitled Query " + lastNum
    }
    lastNum += 1

    val sql = MetadataManager.schema.map { s =>
      if (s.tables.isEmpty) { "" } else { s"select * from ${s.tables(Random.nextInt(s.tables.size)).name} limit 5;" }
    }.getOrElse("")

    workspace.append(SqlEditorTemplate.forQuery(queryId, queryName, sql).toString)

    TabManager.addTab(queryId, queryName, "pencil-square-o")

    val sqlEditor = EditorManager.initSqlEditor(queryId)

    def wire(q: JQuery, action: String) = q.click({ (e: JQueryEventObject) =>
      val sql = sqlEditor.getValue().toString
      utils.Logging.info(s"Performing [$action] for sql [$sql].")
      sendMessage(SubmitQuery(queryId, sql, Some(action)))
      false
    })

    val queryPanel = $(s"#panel-$queryId")

    wire($(".run-query-link", queryPanel), "run")
    wire($(".explain-query-link", queryPanel), "explain")
    wire($(".analyze-query-link", queryPanel), "analyze")

    $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
      QueryManager.closeQuery(queryId, Some(sqlEditor), sendMessage)
      false
    })

    activeQueries = activeQueries :+ queryId
  }

  def closeQuery(queryId: UUID, editor: Option[js.Dynamic], sendMessage: (RequestMessage) => Unit): Unit = {
    if (activeQueries.size == 1) {
      addNewQuery(sendMessage)
    }

    utils.Logging.info(s"Closing [$queryId].")

    val originalIndex = activeQueries.indexOf(queryId)
    activeQueries = activeQueries.filterNot(_ == queryId)

    editor.map(_.destroy())
    $(s"#panel-$queryId").remove()
    TabManager.removeTab(queryId)

    val newId = activeQueries(if (originalIndex == 0) { 0 } else { originalIndex - 1 })
    TabManager.selectTab(newId)
  }
}
