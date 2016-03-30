package ui

import java.util.UUID

import models.SubmitQuery
import models.template.Icons
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout

object QueryManager {
  var activeQueries = Seq.empty[UUID]
  lazy val workspace = $("#workspace")

  def addQuery(queryId: UUID, queryPanel: JQuery, onChange: (String) => Unit, onClose: () => Unit): Unit = {
    val sqlEditor = EditorManager.initSqlEditor(queryId, onChange)

    def wire(q: JQuery, action: String) = q.click({ (e: JQueryEventObject) =>
      val sql = sqlEditor.getValue().toString
      //utils.Logging.info(s"Performing [$action] for sql [$sql].")
      utils.NetworkMessage.sendMessage(SubmitQuery(queryId, sql, Some(action)))
      false
    })

    wire($(".run-query-link", queryPanel), "run")
    wire($(".explain-query-link", queryPanel), "explain")
    wire($(".analyze-query-link", queryPanel), "analyze")

    $(s".save-query-link", queryPanel).click({ (e: JQueryEventObject) =>
      false
    })

    $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
      QueryManager.closeQuery(queryId, Some(sqlEditor))
      false
    })

    sqlEditor.selection.selectAll()
    setTimeout(500) {
      sqlEditor.focus()
    }

    activeQueries = activeQueries :+ queryId
  }

  def closeQuery(queryId: UUID, editor: Option[js.Dynamic]): Unit = {
    if (activeQueries.size == 1) {
      AdHocQueryManager.addNewQuery()
    }

    //utils.Logging.info(s"Closing [$queryId].")

    val originalIndex = activeQueries.indexOf(queryId)
    activeQueries = activeQueries.filterNot(_ == queryId)

    editor.map(_.destroy())
    $(s"#panel-$queryId").remove()
    TabManager.removeTab(queryId)

    val newId = activeQueries(if (originalIndex == 0) { 0 } else { originalIndex - 1 })
    TabManager.selectTab(newId)
  }
}
