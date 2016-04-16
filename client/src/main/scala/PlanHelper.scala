import java.util.UUID

import models._
import models.template._
import org.scalajs.jquery.{ jQuery => $ }
import utils.JQueryUtils

trait PlanHelper {
  this: DatabaseFlow =>
  protected[this] def handlePlanResultResponse(pr: PlanResultResponse) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val occurred = new scalajs.js.Date(pr.result.occurred.toDouble)
    val html = QueryPlanTemplate.forPlan(pr, occurred.toISOString, occurred.toString)
    val workspace = $(s"#workspace-${pr.result.queryId}")
    workspace.prepend(html.toString)

    workOutPlanWidth(pr.id)

    val panel = $(s"#${pr.id}")
    val chart = $(s".plan-chart", panel)
    val raw = $(s".plan-raw", panel)

    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()

    JQueryUtils.clickHandler($("a", panel), _.toggleClass("open"))

    val planViewToggle = $(s".plan-view-toggle", panel)
    var showingChart = true

    JQueryUtils.clickHandler(planViewToggle, (f) => {
      if (showingChart) {
        planViewToggle.text("View Plan Chart")
        chart.hide()
        raw.show()
      } else {
        planViewToggle.text("View Raw Plan")
        chart.show()
        raw.hide()
      }
      showingChart = !showingChart
    })

    utils.JQueryUtils.clickHandler($(s".${Icons.close}", panel), (jq) => panel.remove())
  }

  protected[this] def handlePlanErrorResponse(per: PlanErrorResponse) = {
    val occurred = new scalajs.js.Date(per.error.occurred.toDouble)
    val html = ErrorTemplate.forPlanError(per, occurred.toISOString, occurred.toString)
    val workspace = $(s"#workspace-${per.error.queryId}")
    workspace.prepend(html.toString)

    val panel = $(s"#${per.id}")
    scalajs.js.Dynamic.global.$("time.timeago", panel).timeago()

    utils.JQueryUtils.clickHandler($(s".${Icons.close}", panel), (f) => $(s"#${per.id}").remove())
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
