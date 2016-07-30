package ui.query

import java.util.UUID

import models.CheckQuery
import models.query.SqlParser
import org.scalajs.jquery.JQuery
import ui.EditorCreationHelper
import utils.NetworkMessage

import scala.scalajs.js

object SqlManager {
  var sqlEditors = Map.empty[UUID, js.Dynamic]
  var sqlChecks = Map.empty[UUID, String]

  def newEditor(queryId: UUID, onChange: (String) => Unit) = {
    val editor = EditorCreationHelper.initSqlEditor(queryId, (s: String) => {
      val changed = !sqlChecks.get(queryId).contains(s)
      if (changed) {
        check(queryId)
        onChange(s)
      }
    })
    sqlEditors = sqlEditors + (queryId -> editor)

    editor
  }

  def getLinkTitle(queryId: UUID) = {
    val sqlEditor = sqlEditors.getOrElse(queryId, throw new IllegalStateException(s"Invalid editor for [$queryId]."))
    val sql = sqlEditor.getValue().toString.stripSuffix(";")
    val split = SqlParser.split(sql)
    if (split.length > 1) {
      "Run Active"
    } else {
      "Run"
    }
  }

  def updateLinks(queryId: UUID, runQueryLink: JQuery, runQueryAllLink: JQuery): Unit = {
    val sqlEditor = sqlEditors.getOrElse(queryId, throw new IllegalStateException(s"Invalid editor for [$queryId]."))
    val sql = sqlEditor.getValue().toString.stripSuffix(";")
    val split = SqlParser.split(sql)
    if (split.length > 1) {
      runQueryLink.text("Run Active")
      runQueryAllLink.css("display", "inline")
    } else {
      runQueryLink.text("Run")
      runQueryAllLink.css("display", "none")
    }
  }

  def getEditor(queryId: UUID) = sqlEditors.get(queryId)

  def getSql(queryId: UUID) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.getValue().toString
    case _ => ""
  }

  def getActiveSql(queryId: UUID) = {
    val editor = sqlEditors.getOrElse(queryId, throw new IllegalStateException(s"Missing editor for [$queryId]."))
    val txt = editor.getSelectedText().toString
    if (txt.isEmpty) {
      val sql = editor.getValue().toString.stripSuffix(";")
      val split = SqlParser.split(sql)
      if (split.length > 1) {
        val pos = editor.getCursorPosition()
        val tgtIdx = editor.getSession().getDocument().positionToIndex(pos).toString.toInt
        val idx = split.indexWhere(_._2 > tgtIdx)
        if (idx == -1) {
          split.lastOption.getOrElse(throw new IllegalStateException())._1
        } else {
          split(idx - 1)._1
        }
      } else {
        sql
      }
    } else {
      txt
    }
  }

  def setSql(queryId: UUID, sql: String) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.setValue(sql, editor.getCursorPosition())
    case _ => // no op
  }

  def check(queryId: UUID) = {
    val sql = getActiveSql(queryId)
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
