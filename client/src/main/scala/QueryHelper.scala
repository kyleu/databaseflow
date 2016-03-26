import java.util.UUID

import models.SubmitQuery
import models.template.SqlEditorTemplate
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

import scala.scalajs.js
import scala.util.Random

object QueryHelper {
  case class QueryRecord(queryId: UUID, sqlEditor: js.Dynamic, panel: JQuery, workspace: JQuery)
}

trait QueryHelper { this: DatabaseFlow =>
  var activeQueries = Seq.empty[QueryHelper.QueryRecord]

  private[this] var lastNum = 1

  private[this] lazy val tabBar = $("#query-tabs")
  private[this] lazy val dynamicTabBar = js.Dynamic.global.$("#query-tabs")
  private[this] lazy val workspace = $("#workspace")

  private[this] def addTab(id: UUID, title: String) = {
    tabBar.append(s"""<li id="tab-$id" class="tab col s3">
      <a href="#panel-$id"><i class="fa fa-pencil-square-o"></i> $title</a>
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

  private[this] def closeQuery(queryId: UUID): Unit = {
    if (activeQueries.size == 1) {
      addNewQuery()
    }
    utils.Logging.info(s"Closing [$queryId].")

    val originalIndex = activeQueries.indexWhere(_.queryId == queryId)
    activeQueries = activeQueries.filterNot(_.queryId == queryId)

    $(s"#panel-$queryId").remove()
    $(s"#tab-$queryId").remove()

    dynamicTabBar.tabs()

    val newId = activeQueries(if (originalIndex == 0) { 0 } else { originalIndex - 1 }).queryId
    dynamicTabBar.tabs("select_tab", s"panel-$newId")
  }

  def addNewQuery(): Unit = {
    val queryId = UUID.randomUUID
    val queryName = if (lastNum == 1) {
      "Untitled Query"
    } else {
      "Untitled Query " + lastNum
    }

    if (lastNum == 1) {
      tabBar.addClass("tabs")
    }

    lastNum += 1

    val sql = schema.map { s =>
      if (s.tables.isEmpty) {
        ""
      } else {
        val table = s.tables(Random.nextInt(s.tables.size)).name
        s"select * from $table limit 5;"
      }
    }.getOrElse("")

    workspace.append(SqlEditorTemplate.forQuery(queryId, queryName, sql).toString)

    addTab(queryId, queryName)

    val sqlEditor = initSqlEditor(queryId)

    val queryWorkspace = $(s"#workspace-$queryId")

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
      closeQuery(queryId)
      false
    })

    val record = QueryHelper.QueryRecord(queryId, sqlEditor, queryPanel, queryWorkspace)
    activeQueries = activeQueries :+ record
  }
}
