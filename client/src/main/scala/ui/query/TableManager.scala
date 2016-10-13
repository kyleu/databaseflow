package ui.query

import java.util.UUID

import models.GetTableDetail
import models.query.RowDataOptions
import models.schema.Table
import models.template._
import models.template.tbl.TableDetailTemplate
import org.scalajs.jquery.{jQuery => $}
import ui.metadata.MetadataManager
import ui._
import ui.modal.RowUpdateManager
import utils.{NetworkMessage, TemplateUtils}

object TableManager extends TableDetailHelper {
  private[this] var openTables = Map.empty[String, UUID]

  def addTable(table: Table) = {
    openTables.get(table.name).foreach { uuid =>
      setTableDetails(uuid, table)
    }
  }

  def tableDetail(name: String, options: RowDataOptions) = {
    val qId = openTables.get(name) match {
      case Some(queryId) =>
        TabManager.selectTab(queryId)
        queryId
      case None =>
        val queryId = UUID.randomUUID
        WorkspaceManager.append(TableDetailTemplate.forTable(queryId, name).toString)

        MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
          case Some(table) if table.columns.nonEmpty => setTableDetails(queryId, table)
          case _ => NetworkMessage.sendMessage(GetTableDetail(name))
        }

        def close() = {
          openTables = openTables - name
          QueryManager.closeQuery(queryId)
        }

        TabManager.addTab(queryId, "table-" + name, name, Icons.table, close)

        val queryPanel = $(s"#panel-$queryId")

        QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

        TemplateUtils.clickHandler($(".view-data-link", queryPanel), jq => {
          val newOptions = options.copy(
            offset = None,
            limit = Some(UserManager.rowsReturned),
            orderByCol = MetadataManager.schema.flatMap(_.tables.find(_.name == name)) match {
              case Some(table) if table.columns.nonEmpty => Some(table.columns.headOption.getOrElse(throw new IllegalStateException()).name)
              case _ => None
            }
          )
          RowDataManager.showRowData("table", queryId, name, newOptions, UUID.randomUUID)
        })

        TemplateUtils.clickHandler($(".insert-data-link", queryPanel), jq => {
          val columns = MetadataManager.schema.flatMap(_.tables.find(_.name == name)).map(_.columns).getOrElse(Nil)
          RowUpdateManager.show(name, Nil, columns)
        })

        openTables = openTables + (name -> queryId)
        queryId
    }
    if (options.isFiltered) {
      RowDataManager.showRowData("table", qId, name, options.copy(limit = Some(UserManager.rowsReturned)), UUID.randomUUID)
    }
  }
}
