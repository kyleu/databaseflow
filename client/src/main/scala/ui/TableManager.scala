package ui

import java.util.UUID

import models.query.RowDataOptions
import models.schema.Table
import models.template._
import models.{ GetTableDetail, GetTableRowData }
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object TableManager {
  private[this] var openTables = Map.empty[String, UUID]

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
        case Some(table) => setTableDetails(queryId, table)
        case None => utils.NetworkMessage.sendMessage(GetTableDetail(name))
      }

      TabManager.addTab(queryId, "table-" + name, name, Icons.table)

      val queryPanel = $(s"#panel-$queryId")

      QueryManager.activeQueries = QueryManager.activeQueries :+ queryId

      utils.JQueryUtils.clickHandler($(".view-data-link", queryPanel), (jq) => {
        viewData(queryId, name, RowDataOptions.empty)
      })

      utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
        openTables = openTables - name
        QueryManager.closeQuery(queryId)
      })

      openTables = openTables + (name -> queryId)
  }

  private[this] def viewData(queryId: UUID, name: String, options: RowDataOptions) = {
    utils.NetworkMessage.sendMessage(GetTableRowData(queryId = queryId, name = name, options = options))
  }

  private[this] def setTableDetails(uuid: UUID, table: Table) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing table panel for [$uuid].")
    }

    table.description.map { desc =>
      $(".description", panel).text(desc)
    }

    table.definition.map { definition =>
      import scalatags.Text.all._
      val section = $(".definition-section", panel)
      section.removeClass("initially-hidden")
      $(".section-content", section).html(pre(cls := "pre-wrap")(definition).render)
    }
    if (table.columns.nonEmpty) {
      val section = $(".columns-section", panel)
      section.removeClass("initially-hidden")
      $(".badge", section).html(table.columns.size.toString)
      $(".section-content", section).html(TableColumnDetailTemplate.columnPanel(table.columns).render)
    }
    if (table.indexes.nonEmpty) {
      val section = $(".indexes-section", panel)
      section.removeClass("initially-hidden")
      $(".badge", section).html(table.indexes.size.toString)
      $(".section-content", section).html(TableIndexDetailTemplate.indexPanel(table.indexes).render)
    }
    if (table.foreignKeys.nonEmpty) {
      val section = $(".foreign-keys-section", panel)
      section.removeClass("initially-hidden")
      $(".badge", section).html(table.foreignKeys.size.toString)
      $(".section-content", section).html(TableForeignKeyDetailTemplate.foreignKeyPanel(table.foreignKeys).render)
    }

    scalajs.js.Dynamic.global.$(".collapsible", panel).collapsible()

    utils.Logging.debug(s"Table [${table.name}] loaded.")
  }
}
