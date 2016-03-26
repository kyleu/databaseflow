package ui

import java.util.UUID

import models.RequestMessage
import models.schema.Table
import models.template.TableViewTemplate
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object TableDetailManager {
  var openTables = Map.empty[String, UUID]

  def viewTable(table: Table, sendMessage: (RequestMessage) => Unit) = openTables.get(table.name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      TabManager.initIfNeeded()
      WorkspaceManager.append(TableViewTemplate.forTable(queryId, table).toString)

      TabManager.addTab(queryId, table.name, "folder-open-o")

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      $(".view-data-link", queryPanel).click({ (e: JQueryEventObject) =>
        false
      })

      $(".definition-link", queryPanel).click({ (e: JQueryEventObject) =>
        false
      })

      $(".foreign-keys-link", queryPanel).click({ (e: JQueryEventObject) =>
        false
      })

      $(".indexes-link", queryPanel).click({ (e: JQueryEventObject) =>
        false
      })

      $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
        openTables = openTables - table.name
        QueryManager.closeQuery(queryId, None, sendMessage)
        false
      })

      openTables = openTables + (table.name -> queryId)
  }
}
