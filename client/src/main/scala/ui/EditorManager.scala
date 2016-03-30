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
    val schemaMatches = MetadataManager.schema.map { sch =>
      convertToJs(sch.tables, "table") ++ convertToJs(sch.views, "view") ++ convertToJs(sch.procedures, "procedure")
    }.getOrElse(Nil)
    val engineMatches = MetadataManager.engine.map { eng =>
      convertToJs(eng.builtInFunctions, "function") ++ convertToJs(eng.columnTypes, "type")
    }.getOrElse(Nil)

    callback(null, js.Array(schemaMatches ++ engineMatches: _*))
  }

  def initEditorFramework() = {
    val langTools = js.Dynamic.global.ace.require("ace/ext/language_tools")
    val completer = js.Dynamic.literal(
      getCompletions = completionsFor _
    )
    langTools.addCompleter(completer)
  }

  def initSqlEditor(id: UUID, onChange: (String) => Unit) = {
    val editor = js.Dynamic.global.ace.edit(s"sql-textarea-$id")
    val session = editor.getSession()

    //editor.setTheme("ace/theme/monokai")
    editor.setShowPrintMargin(false)
    editor.setHighlightActiveLine(false)
    editor.setAutoScrollEditorIntoView(false)
    editor.setOptions(js.Dynamic.literal(
      enableBasicAutocompletion = true,
      enableLiveAutocompletion = true,
      minLines = 4,
      maxLines = 1000
    ))
    editor.$blockScrolling = Double.PositiveInfinity

    session.setMode("ace/mode/sql")
    session.setTabSize(2)
    session.on("change", () => {
      onChange(editor.getValue().toString)
    })

    editor
  }
}
