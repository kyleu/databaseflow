package ui.query

import java.util.UUID

import models.query.SqlParser
import models.{CheckQuery, SubmitQuery}
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.{EditorManager, ProgressManager, TabManager}
import utils.NetworkMessage

import scala.scalajs.js

object QueryManager {
  var activeQueries = Seq.empty[UUID]
  var sqlEditors = Map.empty[UUID, js.Dynamic]
  var sqlChecks = Map.empty[UUID, String]

  lazy val workspace = $("#workspace")

  def addQuery(queryId: UUID, title: String, queryPanel: JQuery, onChange: (String) => Unit): Unit = {
    val sqlEditor = EditorManager.initSqlEditor(queryId, (s: String) => {
      val changed = !sqlChecks.get(queryId).contains(s)
      if (changed) {
        check(queryId, s)
        onChange(s)
      }
    })

    def wire(q: JQuery, action: String) = utils.JQueryUtils.clickHandler(q, (jq) => {
      val resultId = UUID.randomUUID
      ProgressManager.startProgress(queryId, resultId, title)
      val sql = {
        val txt = sqlEditor.getSelectedText().toString
        if (txt.isEmpty) {
          val sql = sqlEditor.getValue().toString.stripSuffix(";")
          val split = SqlParser.split(sql)
          if (split.length > 1) {
            val pos = sqlEditor.getCursorPosition()
            val tgtIdx = sqlEditor.getSession().getDocument().positionToIndex(pos).toString.toInt
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
      utils.NetworkMessage.sendMessage(SubmitQuery(queryId, sql, Some(action), resultId))
    })

    def updateName() = {
      val txt = sqlEditor.getSelectedText().toString
      if (txt.isEmpty) {
        val sql = sqlEditor.getValue().toString.stripSuffix(";")
        val split = SqlParser.split(sql)
        if (split.length > 1) {
          $(".run-query-link", queryPanel).text("Run Active")
        } else {
          $(".run-query-link", queryPanel).text("Run")
        }
      } else {
        $(".run-query-link", queryPanel).text("Run Selection")
      }
    }

    wire($(".run-query-link", queryPanel), "run")
    wire($(".explain-query-link", queryPanel), "explain")
    wire($(".analyze-query-link", queryPanel), "analyze")

    sqlEditor.selection.moveCursorFileEnd()
    sqlEditor.selection.on("changeSelection", updateName _)
    sqlEditor.focus()

    activeQueries = activeQueries :+ queryId
    sqlEditors = sqlEditors + (queryId -> sqlEditor)

    updateName()
    check(queryId, getSql(queryId))
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
    if (activeQueries.size == 1) {
      AdHocQueryManager.addNewQuery()
    }

    sqlEditors.get(queryId).foreach(_.destroy())
    $(s"#panel-$queryId").remove()
    TabManager.removeTab(queryId)

    val originalIndex = activeQueries.indexOf(queryId)
    activeQueries = activeQueries.filterNot(_ == queryId)
    sqlEditors = sqlEditors - queryId
    sqlChecks = sqlChecks - queryId

    val newId = activeQueries(if (originalIndex < 1) { 0 } else { originalIndex - 1 })
    TabManager.selectTab(newId)
  }

  def blurEditor(queryId: UUID) = sqlEditors.get(queryId) match {
    case Some(editor) => editor.blur()
    case _ => // no op
  }
}
