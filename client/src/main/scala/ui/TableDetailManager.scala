package ui

import java.util.UUID

import models.{ RequestMessage, ViewTable }
import models.schema.Table
import models.template.TableDetailTemplate
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object TableDetailManager {
  var openTables = Map.empty[String, UUID]

  def tableDetail(table: Table, sendMessage: (RequestMessage) => Unit) = openTables.get(table.name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(TableDetailTemplate.forTable(queryId, table).toString)

      TabManager.addTab(queryId, table.name, "folder-open-o")

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewData(queryId, table, sendMessage)
        false
      })

      $(".foreign-keys-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewForeignKeys(queryId, table)
        false
      })

      $(".indexes-link", queryPanel).click({ (e: JQueryEventObject) =>
        viewIndexes(queryId, table)
        false
      })

      $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
        openTables = openTables - table.name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openTables = openTables + (table.name -> queryId)
  }

  private[this] def viewData(queryId: UUID, table: Table, sendMessage: (RequestMessage) => Unit) = {
    sendMessage(ViewTable(queryId = queryId, name = table.name))
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
}
