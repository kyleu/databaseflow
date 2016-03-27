package ui

import java.util.UUID

import models.{ RequestMessage, ShowTableData }
import models.schema.Table
import models.template.TableDetailTemplate
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NotificationService

object TableDetailManager {
  var tables = Map.empty[String, Table]
  var openTables = Map.empty[String, UUID]

  def tableDetail(name: String, sendMessage: (RequestMessage) => Unit) = openTables.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      if (!tables.isDefinedAt(name)) {
        //sendMessage(???)
      }

      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(TableDetailTemplate.forTable(queryId, name).toString)

      TabManager.addTab(queryId, name, "folder-open-o")

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      def crash() = NotificationService.info("Table Not Loaded", "Please retry in a moment.")

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        tables.get(name) match {
          case Some(table) => viewData(queryId, table, sendMessage)
          case None => crash()
        }
        false
      })

      $(".foreign-keys-link", queryPanel).click({ (e: JQueryEventObject) =>
        tables.get(name) match {
          case Some(table) => viewForeignKeys(queryId, table)
          case None => crash()
        }
        false
      })

      $(".indexes-link", queryPanel).click({ (e: JQueryEventObject) =>
        tables.get(name) match {
          case Some(table) => viewIndexes(queryId, table)
          case None => crash()
        }
        false
      })

      $(".columns-link", queryPanel).click({ (e: JQueryEventObject) =>
        tables.get(name) match {
          case Some(table) => viewColumns(queryId, table)
          case None => crash()
        }
        false
      })

      $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
        openTables = openTables - name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openTables = openTables + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, table: Table, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ShowTableData(queryId = queryId, name = table.name))
  }

  private[this] def viewForeignKeys(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableDetailTemplate.foreignKeysForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .fa-close").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }

  private[this] def viewIndexes(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableDetailTemplate.indexesForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .fa-close").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }

  private[this] def viewColumns(queryId: UUID, table: Table) = {
    val id = UUID.randomUUID
    val html = TableDetailTemplate.columnsForTable(id, queryId, table)
    $(s"#workspace-$queryId").prepend(html.toString)
    $(s"#$id .fa-close").click({ (e: JQueryEventObject) =>
      $(s"#$id").remove()
      false
    })
  }
}
