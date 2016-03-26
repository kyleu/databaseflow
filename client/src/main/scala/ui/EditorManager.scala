package ui

import java.util.UUID

import scala.scalajs.js

object EditorManager {
  def initSqlEditor(id: UUID) = {
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
}
