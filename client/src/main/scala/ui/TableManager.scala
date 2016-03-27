package ui

import java.util.UUID

import models.schema.Table
import models.template.TableDetailTemplate
import models.{ GetTableDetail, RequestMessage, ShowTableData }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object TableManager {
  var tables = Map.empty[String, Table]
  private[this] var openTables = Map.empty[String, UUID]

  def addTable(table: Table) = {
    utils.Logging.info("Done.")
    tables = tables + (table.name -> table)
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

      TabManager.addTab(queryId, name, "folder-open-o")

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, name, sendMessage)
        false
      })

      TableDetailManager.wire(queryPanel, queryId, name, sendMessage)

      $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
        openTables = openTables - name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openTables = openTables + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, name: String, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ShowTableData(queryId = queryId, name = name))
  }
}
