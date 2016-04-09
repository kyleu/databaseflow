package ui

import java.util.UUID

import models.schema.Table
import models.template.{ Icons, TableDetailTemplate }
import models.{ GetTableDetail, GetTableRowData }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object TableManager {
  private[this] var openTables = Map.empty[String, UUID]

  def addTable(table: Table) = {
    openTables.get(table.name).foreach { uuid =>
      setTableDetails(uuid, table)
    }
  }

  def tableDetail(name: String, initialFilter: Option[(String, String, String)] = None) = openTables.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      WorkspaceManager.append(TableDetailTemplate.forTable(queryId, name).toString)

      MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
        case Some(table) => setTableDetails(queryId, table)
        case None => utils.NetworkMessage.sendMessage(GetTableDetail(name))
      }

      TabManager.addTab(queryId, "table-" + name, name, Icons.table)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, name, None)
        false
      })

      TableObjectManager.wire(queryPanel, queryId, name)

      $(s".${Icons.close}", queryPanel).click({ (e: JQueryEventObject) =>
        openTables = openTables - name
        QueryManager.closeQuery(queryId)
        false
      })

      openTables = openTables + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, name: String, filter: Option[(String, String, String)]) = {
    utils.NetworkMessage.sendMessage(GetTableRowData(queryId = queryId, name = name, filter = filter))
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

    if (table.definition.nonEmpty) {
      $(".definition-link", panel).removeClass("initially-hidden")
    }
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
