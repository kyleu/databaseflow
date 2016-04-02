import java.util.UUID

import models._
import models.query.SavedQuery
import models.template._
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NotificationService
import ui._

trait ResultsHelper { this: DatabaseFlow =>
  protected[this] def handleQueryResultResponse(qr: QueryResultResponse) = {
    //Logging.info(s"Received result with [${qr.columns.size}] columns and [${qr.data.size}] rows.")
    val html = QueryResultsTemplate.forResults(qr)
    val workspace = $(s"#workspace-${qr.result.queryId}")
    if (workspace.length != 1) {
      utils.Logging.warn(s"No workspace available for query [${qr.result.queryId}].")
    }
    workspace.prepend(html.toString)

    val panel = $(s"#${qr.id}", workspace)
    val resultEl = $(".query-result-table", workspace)
    val sqlEl = $(".query-result-sql", workspace)
    val sqlLink = $(s".results-sql-link", panel)

    var sqlShown = false

    sqlLink.click((e: JQueryEventObject) => {
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
      false
    })

    $(s".${Icons.close}", panel).click((e: JQueryEventObject) => {
      panel.remove()
    })
  }

  protected[this] def handleQueryErrorResponse(qe: QueryErrorResponse) = {
    val html = QueryErrorTemplate.forError(qe)
    val workspace = $(s"#workspace-${qe.error.queryId}")
    workspace.prepend(html.toString)
    $(s"#${qe.id} .${Icons.close}").click((e: JQueryEventObject) => {
      $(s"#${qe.id}").remove()
    })
  }

  protected[this] def handleTableResultResponse(tr: TableResultResponse) = {
    TableManager.addTable(tr.table)
  }

  protected[this] def handleViewResultResponse(vrr: ViewResultResponse) = {
    ViewManager.addView(vrr.table)
  }

  protected[this] def handleQuerySaveResponse(sq: SavedQuery, error: Option[String]) = error match {
    case Some(err) => NotificationService.info("Query Save Error", err)
    case None => QueryFormManager.handleQuerySaveResponse(sq, error)
  }

  protected[this] def handleQueryDeleteResponse(id: UUID, error: Option[String]) = error match {
    case Some(err) => NotificationService.info("Query Delete Error", err)
    case None => SavedQueryManager.deleteQuery(id)
  }
}
