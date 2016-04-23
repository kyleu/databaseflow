import java.util.UUID

import models._
import models.query.{ QueryResult, RowDataOptions }
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ ProgressManager, TableManager }
import utils.JQueryUtils

trait QueryResultsHelper { this: DatabaseFlow =>
  protected[this] def handleStatementResultResponse(srr: StatementResultResponse) = {
    val occurred = new scalajs.js.Date(srr.result.occurred.toDouble)
    val content = StatementResultsTemplate.forResults(srr, occurred.toISOString, occurred.toString)
    ProgressManager.completeProgress(srr.result.queryId, srr.id, Icons.statementResults, srr.result.title, content, Nil)
  }

  protected[this] def handleQueryResultResponse(qrr: QueryResultResponse) = if (qrr.result.source.forall(_.dataOffset == 0)) {
    handleNewQueryResult(qrr.id, qrr.result, qrr.durationMs)
  } else {
    handleAppendQueryResult(qrr.id, qrr.result, qrr.durationMs)
  }

  private[this] def handleNewQueryResult(resultId: UUID, qr: QueryResult, durationMs: Int) = {
    val occurred = new scalajs.js.Date(qr.occurred.toDouble)
    val content = QueryResultsTemplate.forResults(qr, occurred.toISOString, occurred.toString, durationMs)

    ProgressManager.completeProgress(qr.queryId, resultId, Icons.queryResults, qr.title, content, QueryResultsTemplate.actions)

    val workspace = $(s"#workspace-${qr.queryId}")
    val panel = $(s"#$resultId", workspace)

    val resultEl = $(".query-result-table", panel)
    val sqlEl = $(".query-result-sql", panel)
    val sqlLink = $(s".results-sql-link", panel)

    JQueryUtils.clickHandler($(".query-rel-link", panel), (jq) => {
      val table = jq.data("rel-table").toString
      val col = jq.data("rel-col").toString
      val v = jq.data("rel-val").toString
      TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some("="), filterVal = Some(v)))
    })

    var sqlShown = false

    JQueryUtils.clickHandler(sqlLink, (jq) => {
      if (sqlShown) {
        resultEl.show()
        sqlEl.hide()
        sqlLink.text("Show SQL")
      } else {
        resultEl.hide()
        sqlEl.show()
        sqlLink.text("Show Results")
      }
      sqlShown = !sqlShown
    })
  }

  private[this] def handleAppendQueryResult(resultId: UUID, qr: QueryResult, durationMs: Int) = {
    utils.Logging.info(s"TODO: Appending [${qr.data.length}] rows to result [$resultId].")
  }

  protected[this] def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = ErrorTemplate.forQueryError(qer, occurred.toISOString, occurred.toString)

    ProgressManager.completeProgress(qer.error.queryId, qer.id, Icons.error, "Query Error", content, Nil)
  }
}
