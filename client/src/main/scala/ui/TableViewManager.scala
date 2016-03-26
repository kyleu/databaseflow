package ui

import java.util.UUID

import models.RequestMessage
import models.schema.Table
import models.template.TableViewTemplate
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object TableViewManager {
  var openTables = Map.empty[String, UUID]

  def viewTable(table: Table, sendMessage: (RequestMessage) => Unit) = {
    val queryId = UUID.randomUUID

    TabManager.initIfNeeded()

    WorkspaceManager.append(TableViewTemplate.forTable(queryId, table).toString)

    TabManager.addTab(queryId, table.name, "map-o")

    val queryPanel = $(s"#panel-$queryId")

    QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

    $(".fa-close", queryPanel).click({ (e: JQueryEventObject) =>
      openTables = openTables - table.name
      QueryManager.closeQuery(queryId, None, sendMessage)
      false
    })

    openTables = openTables + (table.name -> queryId)
  }
}
