import models._
import models.query.RowDataOptions
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import ui.TableManager
import utils.JQueryUtils

trait QueryResultsHelper { this: DatabaseFlow =>
  protected[this] def handleStatementResultResponse(srr: StatementResultResponse) = {
    val occurred = new scalajs.js.Date(srr.result.occurred.toDouble)
    val html = StatementResultsTemplate.forResults(srr, occurred.toISOString, occurred.toString)
    val workspace = $(s"#workspace-${srr.result.queryId}")
    if (workspace.length != 1) {
      utils.Logging.warn(s"No workspace available for statement [${srr.result.queryId}].")
    }
    workspace.prepend(html.toString)

    val panel = $(s"#${srr.id}", workspace)
    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()
    JQueryUtils.clickHandler($(s".${Icons.close}", panel), (jq) => panel.remove())
  }

  protected[this] def handleQueryResultResponse(qr: QueryResultResponse) = {
    //Logging.info(s"Received result with [${qr.columns.size}] columns and [${qr.data.size}] rows.")
    val occurred = new scalajs.js.Date(qr.result.occurred.toDouble)
    val html = QueryResultsTemplate.forResults(qr, occurred.toISOString, occurred.toString)
    val workspace = $(s"#workspace-${qr.result.queryId}")
    if (workspace.length != 1) {
      utils.Logging.warn(s"No workspace available for query [${qr.result.queryId}].")
    }
    workspace.prepend(html.toString)

    val panel = $(s"#${qr.id}", workspace)
    val resultEl = $(".query-result-table", panel)
    val sqlEl = $(".query-result-sql", panel)
    val sqlLink = $(s".results-sql-link", panel)

    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()

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

    utils.JQueryUtils.clickHandler($(s".${Icons.close}", panel), (jq) => panel.remove())
  }

  protected[this] def handleQueryErrorResponse(qe: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qe.error.occurred.toDouble)
    val html = ErrorTemplate.forQueryError(qe, occurred.toISOString, occurred.toString)
    val workspace = $(s"#workspace-${qe.error.queryId}")
    workspace.prepend(html.toString)

    val panel = $(s"#${qe.id}")
    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()
    utils.JQueryUtils.clickHandler($(s".${Icons.close}", panel), (jq) => panel.remove())
  }
}
