package ui.query

import java.util.UUID

import models.query.{QueryFilter, QueryResult}
import models.query.QueryResult.Source
import models.schema.{ColumnType, FilterOp}
import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery => $}
import util.{Config, TemplateUtils}

object FilterManager {
  def init(key: String, queryId: UUID, name: String, panel: JQuery, src: Source, columns: Seq[QueryResult.Col], resultId: UUID) = {
    val filterContainer = $(".filter-container", panel)
    var filterShown = false
    TemplateUtils.clickHandler($(".results-filter-link", panel), _ => {
      if (filterShown) { filterContainer.hide() } else { filterContainer.show() }
      filterShown = !filterShown
    })
    TemplateUtils.clickHandler($(".results-filter-cancel", panel), _ => {
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

    TemplateUtils.clickHandler($(".results-filter-go", panel), _ => {
      $(".filter-container", panel).hide()

      val column = $(":selected", colSelect).`val`().toString
      val op = $(":selected", opSelect).`val`().toString
      val t = columns.find(_.name == column) match {
        case Some(col) => col.t
        case None => ColumnType.StringType
      }
      val v = if (op == "btw") {
        doubleValA.`val`().toString + "|" + doubleValB.`val`().toString
      } else {
        singleVal.`val`().toString
      }

      val options = src.copy(filters = Seq(QueryFilter(col = column, op = FilterOp.withName(op), t = t, v = v))).asRowDataOptions(Some(Config.pageSize))

      RowDataManager.showRowData(key, queryId, name, options, resultId)
    })
  }
}
