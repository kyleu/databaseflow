import models._
import models.query.RowDataOptions
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import ui.TableManager
import utils.JQueryUtils

trait QueryResultsHelper { this: DatabaseFlow =>
  protected[this] def handleStatementResultResponse(srr: StatementResultResponse) = {
    val occurred = new scalajs.js.Date(srr.result.occurred.toDouble)
    val status = StatementResultsTemplate.status(srr, occurred.toISOString, occurred.toString)
    val html = StatementResultsTemplate.forResults(srr)

    val workspace = $(s"#workspace-${srr.result.queryId}")
    if (workspace.length != 1) {
      utils.Logging.warn(s"No workspace available for statement [${srr.result.queryId}].")
    }

    val panel = $(s"#${srr.id}", workspace)
    if (panel.length != 1) {
      utils.Logging.warn(s"No panel available for result [${srr.id}].")
    }

    panel.prepend(html.toString)

    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()
    JQueryUtils.clickHandler($(s".${Icons.close}", panel), (jq) => panel.remove())
  }

  protected[this] def handleQueryResultResponse(qr: QueryResultResponse) = {
    val occurred = new scalajs.js.Date(qr.result.occurred.toDouble)
    val desc = QueryResultsTemplate.status(qr, occurred.toISOString, occurred.toString)
    val html = QueryResultsTemplate.forResults(qr)

    val workspace = $(s"#workspace-${qr.result.queryId}")
    if (workspace.length != 1) {
      utils.Logging.warn(s"No workspace available for query [${qr.result.queryId}].")
    }

    val panel = $(s"#${qr.id}", workspace)
    if (panel.length != 1) {
      utils.Logging.warn(s"No panel available for result [${qr.id}].")
    }

    val contentElement = $(".content", panel)
    contentElement.html(html.render)

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

  protected[this] def handleQueryErrorResponse(qer: QueryErrorResponse) = {
    val occurred = new scalajs.js.Date(qer.error.occurred.toDouble)
    val html = ErrorTemplate.forQueryError(qer, occurred.toISOString, occurred.toString)
    val workspace = $(s"#workspace-${qer.error.queryId}")

    val panel = $(s"#${qer.id}", workspace)
    if (panel.length != 1) {
      utils.Logging.warn(s"No panel available for result [${qer.id}].")
    }

    $(".content", panel).html(html.toString)

    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()
    utils.JQueryUtils.clickHandler($(s".${Icons.close}", panel), (jq) => panel.remove())
  }
}
