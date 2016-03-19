import java.util.UUID

import models.template.{ QueryErrorTemplate, QueryPlanTemplate, QueryResultsTemplate }
import models._
import utils.Logging
import org.scalajs.jquery.{ jQuery => $, JQueryEventObject }

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)
    case qr: QueryResultResponse => handleQueryResult(qr)
    case qe: QueryErrorResponse => handleQueryError(qe)
    case pr: PlanResult => handlePlanResult(pr)
    case _ => Logging.info("Received: " + rm.getClass.getSimpleName)
  }

  private[this] def handleQueryResult(qr: QueryResultResponse) = {
    //Logging.info(s"Received result with [${qr.columns.size}] columns and [${qr.data.size}] rows.")
    val html = QueryResultsTemplate.forResults(qr)
    workspace.prepend(html.toString)
    $(s"#${qr.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${qr.id}").remove()
    })
  }

  private[this] def handleQueryError(qe: QueryErrorResponse) = {
    val html = QueryErrorTemplate.forError(qe)
    workspace.prepend(html.toString)
    $(s"#${qe.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${qe.id}").remove()
    })
  }

  private[this] def handlePlanResult(pr: PlanResult) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val html = QueryPlanTemplate.forPlan(pr)
    workspace.prepend(html.toString)

    workOutPlanWidth(pr.id)

    $(s"#${pr.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${pr.id}").remove()
    })
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
    Logging.info(s"Tree: ${tree.length}/$treeWidth, Root Node: ${rootNode.length}/$nodeWidth")
  }
}
