import java.util.UUID

import models._
import models.template._
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

trait PlanHelper { this: DatabaseFlow =>
  protected[this] def handlePlanResultResponse(pr: PlanResultResponse) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val html = QueryPlanTemplate.forPlan(pr)
    val workspace = $(s"#workspace-${pr.result.queryId}")
    workspace.prepend(html.toString)

    workOutPlanWidth(pr.id)

    $(s"#${pr.id} .${Icons.close}").click((e: JQueryEventObject) => {
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
  }
}
