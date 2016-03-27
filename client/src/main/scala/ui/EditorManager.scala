package ui

import java.util.UUID

import scala.scalajs.js

object EditorManager {
  private[this] def convertToJs(keys: Seq[String], meta: String) = keys.map { key =>
    js.Dynamic.literal(
      caption = key,
      value = key,
      meta = meta
    )
  }

  private[this] def completionsFor(editor: js.Any, session: js.Any, pos: js.Any, prefix: String, callback: js.Dynamic) {
    MetadataManager.schema.map { sch =>
      val matches = convertToJs(sch.tables, "table") ++ convertToJs(sch.views, "view") ++ convertToJs(sch.procedures, "procedure")
      callback(null, js.Array(matches: _*))
    }
  }

  def initEditorFramework() = {
    val langTools = js.Dynamic.global.ace.require("ace/ext/language_tools")

    val completer = js.Dynamic.literal(
      getCompletions = completionsFor _
    )

    langTools.addCompleter(completer)
  }

  def initSqlEditor(id: UUID) = {
    val editor = js.Dynamic.global.ace.edit(s"sql-textarea-$id")
    //editor.setTheme("ace/theme/monokai")
    editor.setShowPrintMargin(false)
    editor.setHighlightActiveLine(false)
    editor.setAutoScrollEditorIntoView(false)
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
}
