package services.query

import java.util.UUID

import models.query.{QueryResult, RowDataOptions, SharedResult}
import models.schema.{ColumnType, FilterOp}
import org.scalajs.dom
import org.scalajs.jquery._
import ui.modal.{QueryExportFormManager, RowDetailManager, SharedResultFormManager}
import org.scalajs.jquery.{jQuery => $}
import services.NavigationService
import ui.UserManager
import ui.query.TableManager
import utils.TemplateUtils

object QueryEventHandlers {
  def wireLinks(panel: JQuery, result: QueryResult, chartId: UUID) = {
    val src = result.source.getOrElse(throw new IllegalStateException())
    TemplateUtils.clickHandler($(".results-export-link", panel), jq => {
      QueryExportFormManager.show(result.queryId, src)
    })
    TemplateUtils.clickHandler($(".results-share-link", panel), jq => {
      val chart = ChartService.getSettings(chartId).trim match {
        case x if x.isEmpty => None
        case x => Some(x)
      }
      SharedResultFormManager.show(SharedResult(
        owner = UserManager.userId.getOrElse(throw new IllegalStateException()),
        connectionId = NavigationService.connectionId,
        sql = result.sql,
        source = result.source.getOrElse(throw new IllegalStateException()),
        chart = chart
      ))
    })
    TemplateUtils.changeHandler($(".results-chart-toggle", panel), jq => {
      if (jq.prop("checked").toString == "true") {
        ChartService.showChart(chartId, result.columns, src, panel)
      } else {
        ChartService.showData(panel)
      }
    })
  }

  def wireResults(e: JQuery, qr: QueryResult) = {
    TemplateUtils.clickHandler($(".query-rel-link", e), jq => {
      val table = jq.data("rel-table").toString
      val col = jq.data("rel-col").toString
      val t = ColumnType.withName(jq.data("rel-type").toString)
      val v = jq.data("rel-val").toString
      TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some(FilterOp.Equal), filterType = Some(t), filterVal = Some(v)))
    })

    TemplateUtils.clickHandler($(".view-row-link", e), jq => dataClickHandler(jq, qr))
  }

  private[this] def dataClickHandler(jq: JQuery, result: QueryResult) = {
    val table = result.source.filter(_.t == "table").map(_.name)
    val pk = result.columns.filter(_.primaryKey).map(_.name)

    var tdData = Seq.empty[String]
    $("td", jq.parent().parent()).map { e: dom.Element =>
      val v = $(e).data("v").toString
      if (v != "undefined") {
        tdData = v +: tdData
      }
    }
    val data = result.columns.zip(tdData.reverse)
    //utils.Logging.info(s"Showing [$table] row (PK: ${pk.mkString(", ")}) with data [${data.map(d => d._1.name + ": " + d._2).mkString(", ")}].")
    RowDetailManager.show(table, pk, data)
  }
}
