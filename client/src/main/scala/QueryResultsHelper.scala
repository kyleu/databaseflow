import models._
import models.query.RowDataOptions
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ ProgressManager, TableManager }

trait QueryResultsHelper { this: DatabaseFlow =>
  protected[this] def handleStatementResultResponse(srr: StatementResultResponse) = {
    val occurred = new scalajs.js.Date(srr.result.occurred.toDouble)
    val content = StatementResultsTemplate.forResults(srr, occurred.toISOString, occurred.toString)
    ProgressManager.completeProgress(srr.result.queryId, srr.id, Icons.statementResults, "Statement Result", content, Nil)
  }

  protected[this] def handleQueryResultResponse(qrr: QueryResultResponse) = {
    val occurred = new scalajs.js.Date(qrr.result.occurred.toDouble)
    val content = QueryResultsTemplate.forResults(qrr, occurred.toISOString, occurred.toString)

    ProgressManager.completeProgress(qrr.result.queryId, qrr.id, Icons.queryResults, "Query Result", content, QueryResultsTemplate.actions)

    val workspace = $(s"#workspace-${qrr.result.queryId}")
    val panel = $(s"#${qrr.id}", workspace)

    val resultEl = $(".query-result-table", panel)
    val sqlEl = $(".query-result-sql", panel)
    val sqlLink = $(s".results-sql-link", panel)

    utils.JQueryUtils.clickHandler($(".query-rel-link", panel), (jq) => {
      val table = jq.data("rel-table").toString
      val col = jq.data("rel-col").toString
      val v = jq.data("rel-val").toString
      TableManager.tableDetail(table, RowDataOptions(filterCol = Some(col), filterOp = Some("="), filterVal = Some(v)))
    })

    var sqlShown = false

    utils.JQueryUtils.clickHandler(sqlLink, (jq) => {
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

  protected[this] def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val content = ErrorTemplate.forQueryError(qer, occurred.toISOString, occurred.toString)

    ProgressManager.completeProgress(qer.error.queryId, qer.id, Icons.error, "Query Error", content, Nil)
  }
}
