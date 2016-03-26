import java.util.UUID

import models.SubmitQuery
import models.template.SqlEditorTemplate
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

import scala.scalajs.js

trait QueryHelper { this: DatabaseFlow =>
  var activeQueries = Seq.empty[(UUID, js.Dynamic, JQuery)]

  private[this] var lastNum = 1

  private[this] lazy val tabBar = $("#query-tabs")
  private[this] lazy val dynamicTabBar = js.Dynamic.global.$("#query-tabs")
  private[this] lazy val workspace = $("#workspace")

  private[this] def addTab(id: UUID, title: String) = {
    tabBar.append(s"""<li class="tab col s3">
      <a href="#panel-$id">$title</a>
    </li>""")
    dynamicTabBar.tabs()
    dynamicTabBar.tabs("select_tab", s"panel-$id")
  }

  private[this] def initSqlEditor(id: UUID) = {
    val editor = js.Dynamic.global.ace.edit(s"sql-textarea-$id")
    //editor.setTheme("ace/theme/monokai")
    editor.setShowPrintMargin(false)
    editor.setHighlightActiveLine(false)
    //editor.setAutoScrollEditorIntoView(true)
    editor.getSession().setMode("ace/mode/sql")
    editor.getSession().setTabSize(2)
    editor.setOptions(js.Dynamic.literal(
      enableBasicAutocompletion = true,
      enableLiveAutocompletion = true,
      minLines = 4,
      maxLines = 1000
    ))
    editor
  }

  def addNewQuery() = {
    val queryId = UUID.randomUUID
    val queryName = if (lastNum == 1) {
      "Untitled Query"
    } else {
      "Untitled Query " + lastNum
    }

    if (lastNum == 1) {
      tabBar.addClass("tabs")
      tabBar.show()
    }

    lastNum += 1

    workspace.append(SqlEditorTemplate.forQuery(queryId, queryName).toString)

    addTab(queryId, queryName)

    val sqlEditor = initSqlEditor(queryId)

    val queryWorkspace = $(s"#workspace-$queryId")

    def wire(q: JQuery, action: String) = q.click({ (e: JQueryEventObject) =>
      val sql = sqlEditor.getValue().toString
      utils.Logging.info(s"Performing [$action] for sql [$sql].")
      sendMessage(SubmitQuery(queryId, sql, Some(action)))
      false
    })

    wire($(".run-query-link"), "run")
    wire($(".explain-query-link"), "explain")
    wire($(".analyze-query-link"), "analyze")

    activeQueries = (queryId, sqlEditor, queryWorkspace) +: activeQueries
  }
}
