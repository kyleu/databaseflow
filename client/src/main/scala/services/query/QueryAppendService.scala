package services.query

import java.util.UUID

import models.query.QueryResult
import models.template.results.DataTableTemplate
import org.scalajs.jquery.{jQuery => $}
import scribe.Logging

import scala.util.Random

object QueryAppendService extends Logging {
  def handleAppendQueryResult(resultId: UUID, qr: QueryResult): Unit = {
    val workspace = $(s"#workspace-${qr.queryId}")
    val panel = $(s"#$resultId", workspace)
    val resultEl = $(".query-result-table tbody", panel)
    val key = Random.alphanumeric.take(6).mkString
    val content = {
      val containsRowNum = qr.columns.headOption.exists(_.name == "#")
      val rows = DataTableTemplate.tableRows(qr, key, resultId, containsRowNum)
      rows.map(_.render).mkString("\n")
    }
    resultEl.append(content)

    val newRows = $("." + key, panel)
    QueryEventHandlers.wireResults(newRows, qr)

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

    logger.debug(s"Appended [${qr.data.length}] rows to result [$resultId].")
  }
}
