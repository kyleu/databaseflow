package ui

import java.util.UUID

import org.scalajs.jquery.{ jQuery => $ }

import scala.scalajs.js

object TabManager {
  private[this] var initialized = false

  private[this] lazy val tabBar = $("#query-tabs")
  private[this] lazy val dynamicTabBar = js.Dynamic.global.$("#query-tabs")

  def initIfNeeded() = if (!initialized) {
    tabBar.addClass("tabs")
    initialized = true
  }

  def addTab(id: UUID, title: String, icon: String) = {
    tabBar.append(s"""<li id="tab-$id" class="tab col s3">
      <a href="#panel-$id"><i class="fa fa-$icon"></i> $title</a>
    </li>""")
    $(s".tab .indicator").remove()
    dynamicTabBar.tabs()
    dynamicTabBar.tabs("select_tab", s"panel-$id")
  }

  def removeTab(queryId: UUID) = {
    $(s"#tab-$queryId").remove()
    $(s".tab .indicator").remove()
    dynamicTabBar.tabs()
  }

  def selectTab(queryId: UUID) = {
    dynamicTabBar.tabs("select_tab", s"panel-$queryId")
  }
}
