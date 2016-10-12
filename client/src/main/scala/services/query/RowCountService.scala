package services.query

import models.QueryResultRowCount
import org.scalajs.jquery.{jQuery => $}
import utils.NumberUtils

object RowCountService {
  def handleResultRowCount(qrrc: QueryResultRowCount) = {
    val panel = $(s"#${qrrc.resultId}", $(s"#workspace-${qrrc.queryId}"))
    val rowCountEl = $(".total-row-count", panel)
    if (qrrc.overflow) {
      rowCountEl.text(s" of at least ${NumberUtils.withCommas(qrrc.count)} ")
    } else if (qrrc.count > 100) {
      rowCountEl.text(s" of ${NumberUtils.withCommas(qrrc.count)} total ")
    }
    $(".total-duration", panel).text(NumberUtils.withCommas(qrrc.durationMs))
  }
}
