import java.util.UUID

import models._
import models.query.SavedQuery
import models.template._
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NotificationService
import ui.{ TableManager, ViewManager }
import utils.Logging

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)

    case qr: QueryResultResponse => handleQueryResultResponse(qr)
    case qe: QueryErrorResponse => handleQueryErrorResponse(qe)

    case tr: TableResultResponse => handleTableResultResponse(tr)
    case vrr: ViewResultResponse => handleViewResultResponse(vrr)

    case pr: PlanResultResponse => handlePlanResultResponse(pr)

    case qsr: QuerySaveResponse => handleQuerySaveResponse(qsr.status, qsr.savedQuery)

    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.warn(s"Received unknown message of type [${rm.getClass.getSimpleName}].")
  }

  private[this] def handleQueryResultResponse(qr: QueryResultResponse) = {
    //Logging.info(s"Received result with [${qr.columns.size}] columns and [${qr.data.size}] rows.")
    val html = QueryResultsTemplate.forResults(qr)
    val workspace = $(s"#workspace-${qr.result.queryId}")
    workspace.prepend(html.toString)
    $(s"#${qr.id} .${Icons.close}").click((e: JQueryEventObject) => {
      $(s"#${qr.id}").remove()
    })
  }

  private[this] def handleQueryErrorResponse(qe: QueryErrorResponse) = {
    val html = QueryErrorTemplate.forError(qe)
    val workspace = $(s"#workspace-${qe.error.queryId}")
    workspace.prepend(html.toString)
    $(s"#${qe.id} .${Icons.close}").click((e: JQueryEventObject) => {
      $(s"#${qe.id}").remove()
    })
  }

  private[this] def handlePlanResultResponse(pr: PlanResultResponse) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val html = QueryPlanTemplate.forPlan(pr)
    val workspace = $(s"#workspace-${pr.result.queryId}")
    workspace.prepend(html.toString)

    workOutPlanWidth(pr.id)

    $(s"#${pr.id} .${Icons.close}").click((e: JQueryEventObject) => {
      $(s"#${pr.id}").remove()
    })
  }

  private[this] def handleTableResultResponse(tr: TableResultResponse) = {
    TableManager.addTable(tr.table)
  }

  private[this] def handleViewResultResponse(vrr: ViewResultResponse) = {
    ViewManager.addView(vrr.table)
  }

  private[this] def handleQuerySaveResponse(status: String, sq: SavedQuery) = {
    utils.Logging.info(s"Received save query response with status [$status] for saved query [$sq].")
  }

  private[this] def handleServerError(reason: String, content: String) = {
    NotificationService.error(reason, content)
  }

  private[this] def workOutPlanWidth(id: UUID) = {
    val container = $("#" + id)
    val tree = $(".tree", container)
    val rootNode = $(".root-node", container)

    val originalTreeWidth = tree.width()
    var treeWidth = originalTreeWidth
    var nodeWidth = rootNode.width()
    var continue = true
    while (continue) {
      treeWidth = tree.width(treeWidth + 500).width()
      val newNodeWidth = rootNode.width()
      if (newNodeWidth == nodeWidth) {
        continue = false
      } else {
        nodeWidth = newNodeWidth
      }
    }
    tree.width(nodeWidth + 20)
  }
}
