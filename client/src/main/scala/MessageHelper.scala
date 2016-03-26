import java.util.UUID

import models.template.{ QueryErrorTemplate, QueryPlanTemplate, QueryResultsTemplate }
import models._
import utils.Logging
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NotificationService

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)
    case qr: QueryResultResponse => handleQueryResult(qr)
    case qe: QueryErrorResponse => handleQueryError(qe)
    case pr: PlanResultResponse => handlePlanResult(pr)
    case se: ServerError => handleServerError(se.reason, se.content)
    case _ => Logging.info("Received: " + rm.getClass.getSimpleName)
  }

  private[this] def handleQueryResult(qr: QueryResultResponse) = {
    //Logging.info(s"Received result with [${qr.columns.size}] columns and [${qr.data.size}] rows.")
    val html = QueryResultsTemplate.forResults(qr)
    val workspace = $(s"#workspace-${qr.result.queryId}")
    workspace.prepend(html.toString)
    $(s"#${qr.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${qr.id}").remove()
    })
  }

  private[this] def handleQueryError(qe: QueryErrorResponse) = {
    val html = QueryErrorTemplate.forError(qe)
    val workspace = $(s"#workspace-${qe.error.queryId}")
    workspace.prepend(html.toString)
    $(s"#${qe.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${qe.id}").remove()
    })
  }

  private[this] def handlePlanResult(pr: PlanResultResponse) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val html = QueryPlanTemplate.forPlan(pr)
    val workspace = $(s"#workspace-${pr.result.queryId}")
    workspace.prepend(html.toString)

    workOutPlanWidth(pr.id)

    $(s"#${pr.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${pr.id}").remove()
    })
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
