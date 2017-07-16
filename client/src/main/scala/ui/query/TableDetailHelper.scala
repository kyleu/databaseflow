package ui.query

import java.util.UUID

import models.query.{QueryFilter, RowDataOptions}
import models.schema.{ColumnType, FilterOp, Table}
import models.template.column.TableColumnDetailTemplate
import models.template.tbl.{TableForeignKeyDetailTemplate, TableIndexDetailTemplate}
import org.scalajs.jquery.{jQuery => $}
import ui.modal.ColumnDetailManager
import util.{Logging, NumberUtils}

trait TableDetailHelper {
  protected[this] def setTableDetails(uuid: UUID, table: Table) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing table panel for [$uuid].")
    }

    table.description match {
      case Some(desc) => $(".description", panel).show().text(desc)
      case None => $(".description", panel).hide()
    }
    table.rowCountEstimate match {
      case Some(cnt) =>
        $(".row-count", panel).show()
        $(".row-count span", panel).text(NumberUtils.withCommas(cnt))
      case None => $(".row-count", panel).hide()
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

      ColumnDetailManager.installHandlers($(".column-detail-link", section), table.name)
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

    Logging.debug(s"Table [${table.name}] loaded.")
  }

  def forString(id: String) = id.indexOf("::") match {
    case -1 => TableManager.tableDetail(id, RowDataOptions.empty)
    case x =>
      val name = id.substring(0, x)
      val filter = id.substring(x + 2).split('=')
      val options = if (filter.length > 1) {
        val remaining = filter.tail.mkString("=").split(":")
        val first = remaining.headOption.getOrElse(throw new IllegalStateException())
        val (t, v) = if (remaining.size == 1) {
          ColumnType.StringType -> first
        } else {
          ColumnType.withName(first) -> remaining.tail.mkString(":")
        }
        RowDataOptions(filters = Seq(QueryFilter(col = filter.head, op = FilterOp.Equal, t = t, v = v)))
      } else {
        Logging.info(s"Unable to parse filter [${filter.mkString("=")}].")
        RowDataOptions.empty
      }
      TableManager.tableDetail(name, options)
  }
}
