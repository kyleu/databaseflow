package ui.query

import java.util.UUID

import models.schema.Table
import models.template._
import models.template.tbl.{TableColumnDetailTemplate, TableForeignKeyDetailTemplate, TableIndexDetailTemplate}
import org.scalajs.jquery.{jQuery => $}

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
        $(".row-count span", panel).text(utils.NumberUtils.withCommas(cnt))
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
