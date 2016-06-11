package ui

import java.util.UUID

import services.ShortcutService
import ui.metadata.MetadataManager
import org.scalajs.jquery.{ jQuery => $ }

import scala.scalajs.js

object EditorManager {
  private[this] def convertToJs(keys: Seq[String], meta: String) = keys.map { key =>
    js.Dynamic.literal(
      caption = key,
      value = key,
      meta = meta
    )
  }

  @SuppressWarnings(Array("UnusedMethodParameter"))
  private[this] def completionsFor(editor: js.Any, session: js.Any, pos: js.Any, prefix: String, callback: js.Dynamic) {
    val schemaMatches = MetadataManager.schema.map { sch =>
      convertToJs(sch.tables.map(_.name), "table") ++ convertToJs(sch.views.map(_.name), "view") ++ convertToJs(sch.procedures.map(_.name), "procedure")
    }.getOrElse(Nil)
    val engineMatches = MetadataManager.engine.map { eng =>
      convertToJs(eng.builtInFunctions, "function") ++ convertToJs(eng.columnTypes, "type")
    }.getOrElse(Nil)

    callback(utils.NullUtils.inst, js.Array(schemaMatches ++ engineMatches: _*))
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

  def onSave(id: UUID) = {
    val queryPanel = $(s"#panel-$id")
    val saveLink = $(".save-query-link", queryPanel)
    if (saveLink.length != 1) {
      utils.Logging.warn(s"Found [${queryPanel.length}] panels and [${saveLink.length}] links for query [$id].")
    }
    saveLink.click()
  }

  def onRun(id: UUID) = {
    val queryPanel = $(s"#panel-$id")
    val runLink = $(".run-query-link", queryPanel)
    if (runLink.length != 1) {
      utils.Logging.warn(s"Found [${queryPanel.length}] panels and [${runLink.length}] links for query [$id].")
    }
    runLink.click()
  }
}
