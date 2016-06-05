package ui

import java.util.UUID

import models.template.Icons
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import utils.NullUtils

import scala.scalajs.js

object TabManager {
  private[this] var initialized = false
  private[this] var openTabs = Seq.empty[(UUID, String, () => Unit)]

  private[this] lazy val tabBar = $("#query-tabs")
  private[this] lazy val dynamicTabBar = js.Dynamic.global.$("#query-tabs")

  def initIfNeeded() = if (!initialized) {
    $("#tab-loading").remove()
    initialized = true

    $("ul.tabs").on("click", "a", (e: JQueryEventObject) => {
      val queryId = UUID.fromString($(e.currentTarget).data("query").toString)
      openTabs.find(_._1 == queryId) match {
        case Some(x) => org.scalajs.dom.window.history.replaceState(NullUtils.inst, x._2, "#" + x._2)
        case None => throw new IllegalStateException(s"No open tab [$queryId] from choices [${openTabs.mkString(", ")}].")
      }
    })
  }

  def addTab(id: UUID, ctx: String, title: String, icon: String, onClose: () => Unit) = {
    openTabs = openTabs :+ ((id, ctx, onClose))
    tabBar.append(s"""<li id="tab-$id" class="tab col s3">
      <a data-query="$id" href="#panel-$id"><i class="fa $icon"></i> $title</a>
    </li>""")
    $(".tabs .indicator").remove()
    dynamicTabBar.tabs()

    val queryPanel = $(s"#panel-$id")
    utils.JQueryUtils.clickHandler($(s".${Icons.close}", queryPanel), (jq) => {
      onClose()
    })

    selectTab(id)
  }

  def removeTab(queryId: UUID) = {
    val tabCloseOp = openTabs.find(_._1 == queryId).map(_._3)
    openTabs = openTabs.filterNot(_._1 == queryId)
    tabCloseOp.foreach { _() }
    $(s"#tab-$queryId").remove()
    $(".tabs .indicator").remove()
    dynamicTabBar.tabs()
  }

  def selectNextTab() = {
    utils.Logging.info("Moving to next tab...")
  }

  def selectPreviousTab() = {
    utils.Logging.info("Moving to previous tab...")
  }

  def selectTab(queryId: UUID) = {
    dynamicTabBar.tabs("select_tab", s"panel-$queryId")
  }
}
