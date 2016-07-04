package ui.query

import java.util.UUID

import models.query.QueryResult.Source
import models.schema.FilterOp
import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery => $}

object FilterManager {
  def init(key: String, queryId: UUID, name: String, panel: JQuery, src: Source) = {
    val filterContainer = $(".filter-container", panel)
    var filterShown = false
    utils.JQueryUtils.clickHandler($(".results-filter-link", panel), (jq) => {
      if (filterShown) { filterContainer.hide() } else { filterContainer.show() }
      filterShown = !filterShown
    })
    utils.JQueryUtils.clickHandler($(".results-filter-cancel", panel), (jq) => {
      $(".filter-container", panel).hide()
    })

    val single = $(".single-value", panel)
    val double = $(".double-value", panel)

    val colSelect = $(".filter-col-select", panel)
    val opSelect = $(".filter-op-select", panel)
    val singleVal = $(".filter-single-val", panel)
    val doubleValA = $(".filter-double-val-a", panel)
    val doubleValB = $(".filter-double-val-b", panel)

    opSelect.change { (e: JQueryEventObject) =>
      val current = $(e.currentTarget).value().toString
      current match {
        case "" => // no op?
        case "nl" | "nnl" =>
          single.hide()
          double.hide()
        case "btw" =>
          single.hide()
          double.show()
        case _ =>
          single.show()
          double.hide()
      }
    }

    utils.JQueryUtils.clickHandler($(".results-filter-go", panel), (jq) => {
      $(".row-status-display", panel).show()
      $(".filter-container", panel).hide()

      val column = $(":selected", colSelect).`val`().toString
      val op = $(":selected", opSelect).`val`().toString
      val v = if (op == "btw") {
        doubleValA.`val`().toString + "|" + doubleValB.`val`().toString
      } else {
        singleVal.`val`().toString
      }

      val options = src.copy(
        filterColumn = Some(column),
        filterOp = Some(FilterOp.withName(op)),
        filterValue = Some(v)
      ).asRowDataOptions

      RowDataManager.showRowData(key, queryId, name, options)
    })
  }
}
