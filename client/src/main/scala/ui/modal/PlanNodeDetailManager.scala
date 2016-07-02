package ui.modal

import models.plan.PlanNode
import models.template.query.QueryPlanNodeDetailTemplate
import org.scalajs.jquery.{jQuery => $}

import scala.scalajs.js

object PlanNodeDetailManager {
  private[this] val modal = js.Dynamic.global.$("#plan-node-modal")

  private[this] val modalContent = $("#plan-node-modal-content", modal)
  private[this] val modalLink = $("#plan-node-ok-link", modal)

  def init() = {
    utils.JQueryUtils.clickHandler(modalLink, (jq) => {
      close()
    })
  }

  def show(node: PlanNode, totalCost: Int) = {
    val content = QueryPlanNodeDetailTemplate.forNode(node, totalCost)
    modalContent.html(content.toString)
    modal.openModal()
  }

  def close(): Unit = modal.closeModal()
}
