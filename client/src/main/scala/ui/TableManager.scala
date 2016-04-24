package ui

import java.util.UUID

import models.query.RowDataOptions
import models.schema.Table
import models.template._
import models.{ GetTableDetail, GetTableRowData }
import org.scalajs.jquery.{ jQuery => $ }
import utils.JQueryUtils

object TableManager extends TableDetailHelper{
  private[this] var openTables = Map.empty[String, UUID]

  private[this] lazy val workspace = $("#workspace")

  def addTable(table: Table) = {
    openTables.get(table.name).foreach { uuid =>
      setTableDetails(uuid, table)
    }
  }

  def tableDetail(name: String, options: RowDataOptions) = openTables.get(name) match {
    case Some(queryId) =>
      TabManager.selectTab(queryId)
    case None =>
      val queryId = UUID.randomUUID
      WorkspaceManager.append(TableDetailTemplate.forTable(queryId, name).toString)

      MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
        case Some(table) if table.columns.nonEmpty => setTableDetails(queryId, table)
        case _ => utils.NetworkMessage.sendMessage(GetTableDetail(name))
      }

      TabManager.addTab(queryId, "table-" + name, name, Icons.table)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      utils.JQueryUtils.clickHandler($(".view-data-link", queryPanel), (jq) => {
        viewData(queryId, name, RowDataOptions(limit = Some(100)))
      })

      utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
        openTables = openTables - name
        QueryManager.closeQuery(queryId)
      })

      openTables = openTables + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, name: String, options: RowDataOptions): Unit = {
    val resultId = UUID.randomUUID

    def onComplete(): Unit = {
      val panel = $(s"#$resultId")
      if (panel.length != 1) {
        throw new IllegalStateException(s"Found [${panel.length}] panels for result [$resultId].")
      }
      JQueryUtils.clickHandler($(".sorted-title", panel), (j) => {
        val col = j.data("col").toString
        val asc = j.data("dir").toString == "asc"

        viewData(queryId, name, options.copy(
          orderByCol = Some(col),
          orderByAsc = Some(!asc)
        ))
      })
    }

    ProgressManager.startProgress(queryId, resultId, onComplete, Icons.loading, name)
    utils.NetworkMessage.sendMessage(GetTableRowData(queryId = queryId, name = name, options = options, resultId = resultId))
  }
}
