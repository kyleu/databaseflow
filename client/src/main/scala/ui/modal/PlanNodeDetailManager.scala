package ui.modal

import models.plan.PlanNode
import models.template.query.QueryPlanNodeDetailTemplate
import org.scalajs.jquery.{jQuery => $}
import utils.TemplateUtils

import scala.scalajs.js

object PlanNodeDetailManager {
  private[this] val modal = js.Dynamic.global.$("#plan-node-modal")

  private[this] val modalContent = $("#plan-node-modal-content", modal)
  private[this] val modalLink = $("#plan-node-ok-link", modal)

  def init() = TemplateUtils.clickHandler(modalLink, (jq) => {
    close()
  })

  def show(node: PlanNode, total: Either[Int, Double]) = {
    val content = QueryPlanNodeDetailTemplate.forNode(node, total)
    modalContent.html(content.toString)
    modal.openModal()
  }

  def close(): Unit = modal.closeModal()
}
