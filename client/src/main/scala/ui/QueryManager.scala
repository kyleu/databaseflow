package ui

import java.util.UUID

import models.{ CheckQuery, SubmitQuery }
import models.template.Icons
import org.scalajs.jquery.{ JQuery, jQuery => $ }
import utils.NetworkMessage

import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout

object QueryManager {
  var activeQueries = Seq.empty[UUID]
  var sqlEditors = Map.empty[UUID, js.Dynamic]
  var sqlChecks = Map.empty[UUID, String]

  lazy val workspace = $("#workspace")

  def addQuery(queryId: UUID, title: String, queryPanel: JQuery, onChange: (String) => Unit, onClose: () => Unit): Unit = {
    val sqlEditor = EditorManager.initSqlEditor(queryId, (s: String) => {
      val changed = !sqlChecks.get(queryId).contains(s)
      if (changed) {
        check(queryId, s)
        onChange(s)
      }
    })

    def wire(q: JQuery, action: String) = utils.JQueryUtils.clickHandler(q, (jq) => {
      val resultId = UUID.randomUUID

      ProgressManager.startProgress(queryId, resultId, () => Unit, title)

      val sql = sqlEditor.getValue().toString
      utils.NetworkMessage.sendMessage(SubmitQuery(queryId, sql, Some(action), resultId))
    })

    wire($(".run-query-link", queryPanel), "run")
    wire($(".explain-query-link", queryPanel), "explain")
    wire($(".analyze-query-link", queryPanel), "analyze")

    utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
      QueryManager.closeQuery(queryId)
      onClose()
    })

    sqlEditor.selection.selectAll()
    setTimeout(500) {
      sqlEditor.focus()
    }

    activeQueries = activeQueries :+ queryId
    sqlEditors = sqlEditors + (queryId -> sqlEditor)

    check(queryId, getSql(queryId))
  }

  def getSql(queryId: UUID) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.getValue().toString
    case None => ""
  }

  def check(queryId: UUID, sql: String) = {
    sqlChecks = sqlChecks + (queryId -> sql)
    NetworkMessage.sendMessage(CheckQuery(queryId, sql))
  }

  def closeQuery(queryId: UUID): Unit = {
    if (activeQueries.size == 1) {
      AdHocQueryManager.addNewQuery()
    }

    sqlEditors.get(queryId).foreach(_.destroy())
    $(s"#panel-$queryId").remove()
    TabManager.removeTab(queryId)

    val originalIndex = activeQueries.indexOf(queryId)
    activeQueries = activeQueries.filterNot(_ == queryId)
    sqlEditors = sqlEditors - queryId
    sqlChecks = sqlChecks - queryId

    val newId = activeQueries(if (originalIndex < 1) { 0 } else { originalIndex - 1 })
    TabManager.selectTab(newId)
  }
}
