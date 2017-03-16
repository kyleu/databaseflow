package ui

import java.util.UUID

import models.template.Icons
import org.scalajs.jquery.{JQueryEventObject, jQuery => $}
import utils.{NullUtils, TemplateUtils}

import scala.scalajs.js

object TabManager {
  private[this] var initialized = false
  private[this] var openTabs = Seq.empty[(UUID, String, () => Unit)]
  private[this] var activeTab: Option[UUID] = None

  private[this] lazy val tabContainer = $(".tab-container")
  private[this] lazy val mainEl = $("main")
  private[this] lazy val tabBar = $("#query-tabs", tabContainer)
  private[this] lazy val dynamicTabBar = js.Dynamic.global.$("#query-tabs")

  private[this] val tabOffsets = collection.mutable.HashMap.empty[UUID, Double]

  private[this] def transitionTo(query: UUID) = {
    activeTab.foreach { current =>
      if (current != query) {
        tabOffsets(current) = org.scalajs.dom.document.body.scrollTop
      }
    }
    activeTab = Some(query)

    tabOffsets.get(query).foreach { offset =>
      // TODO Account for document smaller than scrollTop
      org.scalajs.dom.document.body.scrollTop = offset
    }

    openTabs.find(_._1 == query) match {
      case Some(x) => org.scalajs.dom.window.history.replaceState(NullUtils.inst, x._2, "#" + x._2)
      case None => throw new IllegalStateException(s"No open tab [$query] from choices [${openTabs.mkString(", ")}].")
    }
  }

  def initIfNeeded() = if (!initialized) {
    $("#tab-loading").remove()
    initialized = true
    $("ul.tabs").on("click", "a", (e: JQueryEventObject) => {
      val queryId = UUID.fromString($(e.currentTarget).data("query").toString)
      transitionTo(queryId)
    })
  }

  def getActiveTab = activeTab
  def tabCount = openTabs.size

  def addTab(id: UUID, ctx: String, title: String, icon: String, onClose: () => Unit) = {
    openTabs = openTabs :+ ((id, ctx, onClose))
    if (openTabs.length == 1) { hide() } else { show() }
    tabBar.append(s"""<li id="tab-$id" class="tab"><a data-query="$id" href="#panel-$id"><i class="fa $icon"></i> $title</a></li>""")
    $(".tabs .indicator").remove()
    dynamicTabBar.tabs()
    val queryPanel = $(s"#panel-$id")
    TemplateUtils.clickHandler($(s".${Icons.close}", queryPanel), jq => {
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
    if (openTabs.length < 2) { hide() } else { show() }
    if (openTabs.nonEmpty) { dynamicTabBar.tabs() }
  }

  def selectNextTab() = activeTab match {
    case Some(active) =>
      val idx = openTabs.indexWhere(_._1 == active)
      if (idx == -1 || openTabs.length <= 1) {
        openTabs.headOption.foreach(x => selectTab(x._1))
      } else if (idx == openTabs.length - 1) {
        selectTab(openTabs.headOption.getOrElse(throw new IllegalStateException())._1)
      } else {
        selectTab(openTabs(idx + 1)._1)
      }
    case None => openTabs.headOption.foreach(x => selectTab(x._1))
  }

  def selectPreviousTab() = activeTab match {
    case Some(active) =>
      val idx = openTabs.indexWhere(_._1 == active)
      if (idx == -1 || openTabs.length <= 1) {
        openTabs.headOption.foreach(x => selectTab(x._1))
      } else if (idx == 0) {
        selectTab(openTabs.last._1)
      } else {
        selectTab(openTabs(idx - 1)._1)
      }
    case None => openTabs.headOption.foreach(x => selectTab(x._1))
  }

  def selectTab(queryId: UUID) = {
    transitionTo(queryId)
    dynamicTabBar.tabs("select_tab", s"panel-$queryId")
  }

  private[this] def hide() = {
    mainEl.removeClass("margin")
    tabContainer.hide()
  }

  private[this] def show() = {
    mainEl.addClass("margin")
    tabContainer.show()
  }
}
