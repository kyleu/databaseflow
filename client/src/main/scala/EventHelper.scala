import models.SubmitQuery
import org.scalajs.dom
import org.scalajs.dom.Element

import scala.scalajs.js

trait EventHelper { this: DatabaseFlow =>
  protected[this] def wireEvents() = {
    js.Dynamic.global.ace.require("ace/ext/language_tools")
    val sqlEditor = js.Dynamic.global.ace.edit("sql-textarea")
    //editor.setTheme("ace/theme/monokai")
    sqlEditor.setShowPrintMargin(false)
    sqlEditor.setHighlightActiveLine(false)
    //editor.setAutoScrollEditorIntoView(true)
    sqlEditor.getSession().setMode("ace/mode/sql")
    sqlEditor.getSession().setTabSize(2)
    sqlEditor.setOptions(js.Dynamic.literal(
      enableBasicAutocompletion = true,
      enableLiveAutocompletion = true,
      minLines = 4,
      maxLines = 1000
    ))

    def wire(e: Element, action: String) = {
      e.addEventListener("click", { (ev: dom.Event) =>
        action match {
          case "run" | "explain" | "analyze" =>
            val sql = sqlEditor.getValue().toString
            utils.Logging.info(s"Running sql [$sql].")
            sendMessage(SubmitQuery(sql, Some(action)))
          case _ => throw new IllegalArgumentException(s"Unknown element [${ev.toString}].")
        }
      }, false)
    }

    val runQueryLink = dom.document.getElementById("run-query-link")
    wire(runQueryLink, "run")

    val explainQueryLink = dom.document.getElementById("explain-query-link")
    wire(explainQueryLink, "explain")

    val analyzeQueryLink = dom.document.getElementById("analyze-query-link")
    wire(analyzeQueryLink, "analyze")
  }
}
