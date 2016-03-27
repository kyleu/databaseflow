package ui

import java.util.UUID

import models.schema.Table
import models.template.{ Icons, TableDetailTemplate }
import models.{ GetTableDetail, RequestMessage, ShowTableData }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object TableManager {
  var tables = Map.empty[String, Table]
  private[this] var openTables = Map.empty[String, UUID]

  def addTable(table: Table) = {
    tables = tables + (table.name -> table)
    openTables.get(table.name).foreach { uuid =>
      setTableDetails(uuid, table)
    }
  }

  def tableDetail(name: String, sendMessage: (RequestMessage) => Unit) = openTables.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      if (!tables.isDefinedAt(name)) {
        sendMessage(GetTableDetail(name))
      }

      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(TableDetailTemplate.forTable(queryId, name).toString)

      TabManager.addTab(queryId, name, Icons.table)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, name, sendMessage)
        false
      })

      TableObjectManager.wire(queryPanel, queryId, name, sendMessage)

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openTables = openTables - name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openTables = openTables + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, name: String, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ShowTableData(queryId = queryId, name = name))
  }

  private[this] def setTableDetails(uuid: UUID, table: Table) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing table panel for [$uuid].")
    }

    table.description.map { desc =>
      $(".description", panel).text(desc)
    }

    val summary = s"Table contains ${table.columns.size} columns, ${table.indexes.size} indexes, and ${table.foreignKeys.size} foreign keys."
    $(".summary", panel).text(summary)

    if (table.columns.nonEmpty) {
      $(".columns-link", panel).removeClass("initially-hidden")
    }
    if (table.indexes.nonEmpty) {
      $(".indexes-link", panel).removeClass("initially-hidden")
    }
    if (table.foreignKeys.nonEmpty) {
      $(".foreign-keys-link", panel).removeClass("initially-hidden")
    }

    utils.Logging.debug(s"Table [${table.name}] loaded.")
  }
}
