import models.SubmitQuery
import utils.Logging
import org.scalajs.jquery.{ jQuery => $, JQuery, JQueryEventObject }

import scala.scalajs.js

trait InitHelper { this: DatabaseFlow =>
  private[this] var sqlEditor: Option[js.Dynamic] = None

  protected[this] def init() {
    if (sqlEditor.nonEmpty) {
      throw new IllegalArgumentException("Already initialized.")
    }

    sqlEditor = Some(initSqlEditor())
    wireEvents()

    Logging.info("Database Flow Started.")
    connect()
  }

  private[this] def initSqlEditor() = {
    js.Dynamic.global.ace.require("ace/ext/language_tools")
    val editor = js.Dynamic.global.ace.edit("sql-textarea")
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

  private[this] def wireEvents() = {
    def wire(q: JQuery, action: String) = q.click({ (e: JQueryEventObject) =>
      val sql = sqlEditor.map(_.getValue().toString).getOrElse("No Text Provided.")
      utils.Logging.info(s"Performing [$action] for sql [$sql].")
      sendMessage(SubmitQuery(sql, Some(action)))
      false
    })

    wire($("#run-query-link"), "run")
    wire($("#explain-query-link"), "explain")
    wire($("#analyze-query-link"), "analyze")
  }
}
