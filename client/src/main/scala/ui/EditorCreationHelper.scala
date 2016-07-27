package ui

import java.util.UUID

import services.ShortcutService
import ui.metadata.MetadataManager

import scala.scalajs.js

object EditorCreationHelper {
  private[this] def convertToJs(keys: Seq[String], meta: String) = keys.map { key =>
    js.Dynamic.literal(
      caption = key,
      value = key,
      meta = meta
    )
  }

  @SuppressWarnings(Array("UnusedMethodParameter"))
  private[this] def completionsFor(editor: js.Dynamic, session: js.Any, pos: js.Any, prefix: String, callback: js.Dynamic) {
    val schemaMatches = MetadataManager.schema.map { sch =>
      convertToJs(sch.tables.map(_.name), "table") ++ convertToJs(sch.views.map(_.name), "view") ++ convertToJs(sch.procedures.map(_.name), "procedure")
    }.getOrElse(Nil)
    val engineMatches = MetadataManager.engine.map { eng =>
      convertToJs(eng.builtInFunctions, "function") ++ convertToJs(eng.columnTypes, "type")
    }.getOrElse(Nil)
    val content = editor.getValue().toString
    val tableMatches = MetadataManager.schema.map { sch =>
      sch.tables.flatMap { t =>
        if (content.contains(t.name)) {
          Some(convertToJs(t.columns.map(_.name), "column"))
        } else {
          None
        }
      }.flatten
    }.getOrElse(Nil)
    callback(utils.NullUtils.inst, js.Array(schemaMatches ++ engineMatches ++ tableMatches: _*))
  }

  def initEditorFramework() = {
    val langTools = js.Dynamic.global.ace.require("ace/ext/language_tools")
    val completer = js.Dynamic.literal(
      getCompletions = completionsFor _
    )
    langTools.addCompleter(completer)
  }

  @SuppressWarnings(Array("LooksLikeInterpolatedString"))
  def initSqlEditor(id: UUID, onChange: (String) => Unit) = {
    val editor = js.Dynamic.global.ace.edit(s"sql-textarea-$id")
    val session = editor.getSession()

    editor.setShowPrintMargin(false)
    editor.setHighlightActiveLine(false)
    editor.setAutoScrollEditorIntoView(false)
    editor.setFontSize(14)
    editor.setOptions(js.Dynamic.literal(
      enableBasicAutocompletion = true,
      enableLiveAutocompletion = true,
      minLines = 4,
      maxLines = 1000
    ))
    editor.$blockScrolling = Double.PositiveInfinity

    session.setMode("ace/mode/sql")
    session.setTabSize(2)
    session.on("change", () => onChange(editor.getValue().toString))

    ShortcutService.configureEditor(id)

    editor
  }
}
