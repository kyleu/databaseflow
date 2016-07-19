package ui.query

import java.util.UUID

import models.CheckQuery
import models.query.SqlParser
import ui.EditorManager
import utils.NetworkMessage

import scala.scalajs.js

object SqlManager {
  var sqlEditors = Map.empty[UUID, js.Dynamic]
  var sqlChecks = Map.empty[UUID, String]

  def newEditor(queryId: UUID, onChange: (String) => Unit) = {
    val editor = EditorManager.initSqlEditor(queryId, (s: String) => {
      val changed = !sqlChecks.get(queryId).contains(s)
      if (changed) {
        check(queryId, s)
        onChange(s)
      }
    })
    sqlEditors = sqlEditors + (queryId -> editor)

    editor
  }

  def getLinkTitle(queryId: UUID) = {
    val sqlEditor = sqlEditors.getOrElse(queryId, throw new IllegalStateException(s"Invalid editor for [$queryId]."))
    val txt = sqlEditor.getSelectedText().toString
    if (txt.isEmpty) {
      val sql = sqlEditor.getValue().toString.stripSuffix(";")
      val split = SqlParser.split(sql)
      if (split.length > 1) {
        "Run Active"
      } else {
        "Run"
      }
    } else {
      "Run Selection"
    }
  }

  def getSql(queryId: UUID) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.getValue().toString
    case _ => ""
  }

  def setSql(queryId: UUID, sql: String) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.setValue(sql, editor.getCursorPosition())
    case _ => // no op
  }

  def check(queryId: UUID, sql: String) = {
    sqlChecks = sqlChecks + (queryId -> sql)
    NetworkMessage.sendMessage(CheckQuery(queryId, sql))
  }

  def closeQuery(queryId: UUID): Unit = {
    sqlEditors.get(queryId).foreach(_.destroy())
    sqlEditors = sqlEditors - queryId
    sqlChecks = sqlChecks - queryId
  }

  def blurEditor(queryId: UUID) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.blur()
    case _ => // no op
  }
}
