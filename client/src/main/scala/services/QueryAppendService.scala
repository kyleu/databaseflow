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
    val additionalResults = $(".additional-results .no-rows-remaining")
    if (qr.moreRowsAvailable) {
      val limit = appendLink.data("limit").toString.toInt * 2
      appendLink.show()
      appendLink.data("limit", limit)
      appendLink.text(s"Load $limit More Rows")
      additionalResults.hide()
    } else {
      appendLink.hide()
      additionalResults.show()
    }

    utils.Logging.info(s"Appended [${qr.data.length}] rows to result [$resultId].")
  }
}
