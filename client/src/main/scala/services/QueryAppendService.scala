package services

import java.util.UUID

import models.query.{ QueryResult, RowDataOptions }
import models.schema.FilterOp
import models.template.QueryResultsTemplate
import org.scalajs.jquery.{ jQuery => $ }
import ui.TableManager
import utils.JQueryUtils

object QueryAppendService {
  def handleAppendQueryResult(resultId: UUID, qr: QueryResult, durationMs: Int): Unit = {
    val workspace = $(s"#workspace-${qr.queryId}")
    val panel = $(s"#$resultId", workspace)
    val resultEl = $(".query-result-table tbody", panel)

    val content = QueryResultsTemplate.forAppend(qr, resultId)
    resultEl.append(content)

    val newRows = $(s".result-$resultId", panel)

    JQueryUtils.clickHandler($(".query-rel-link", newRows), (jq) => {
      val table = jq.data("rel-table").toString
      val col = jq.data("rel-col").toString
      val v = jq.data("rel-val").toString
      TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some(FilterOp.Equal), filterVal = Some(v)))
    })

    val appendLink = $(".additional-results .append-rows-link")
    if (qr.moreRowsAvailable) {
      appendLink.show()
      $(".additional-results .no-rows-remaining").hide()
    } else {
      $(".additional-results .append-rows-link").hide()
      $(".additional-results .no-rows-remaining").show()
    }

    utils.Logging.info(s"Appended [${qr.data.length}] rows to result [$resultId].")
  }
}
