package ui.query

import java.util.UUID

import models.schema.View
import models.template.column.ViewColumnDetailTemplate
import org.scalajs.jquery.{jQuery => $}
import scribe.Logging
import ui.modal.ColumnDetailManager

trait ViewDetailHelper extends Logging {
  protected[this] def setViewDetails(uuid: UUID, view: View) = {
    val panel = $(s"#panel-$uuid")
    if (panel.length != 1) {
      throw new IllegalStateException(s"Missing view panel for [$uuid].")
    }

    view.description.map { desc =>
      $(".description", panel).text(desc)
    }

    view.definition.map { definition =>
      import scalatags.Text.all._
      val section = $(".definition-section", panel)
      section.removeClass("initially-hidden")
      $(".section-content", section).html(pre(cls := "pre-wrap")(definition).render)
    }
    if (view.columns.nonEmpty) {
      val section = $(".columns-section", panel)
      section.removeClass("initially-hidden")
      $(".badge", section).html(view.columns.size.toString)
      $(".section-content", section).html(ViewColumnDetailTemplate.columnPanel(view.columns).render)

      ColumnDetailManager.installHandlers($(".column-detail-link", section), view.name)
    }

    scalajs.js.Dynamic.global.$(".collapsible", panel).collapsible()

    logger.debug(s"View [${view.name}] loaded.")
  }
}
