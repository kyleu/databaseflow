package ui.tabs

object TabSelectionManager {
  def selectNextTab() = TabManager.getActiveTab match {
    case Some(active) =>
      val idx = TabManager.openTabs.indexWhere(_._1 == active)
      if (idx == -1 || TabManager.openTabs.length <= 1) {
        TabManager.openTabs.headOption.foreach(x => TabManager.selectTab(x._1))
      } else if (idx == TabManager.openTabs.length - 1) {
        TabManager.selectTab(TabManager.openTabs.headOption.getOrElse(throw new IllegalStateException())._1)
      } else {
        TabManager.selectTab(TabManager.openTabs(idx + 1)._1)
      }
    case None => TabManager.openTabs.headOption.foreach(x => TabManager.selectTab(x._1))
  }

  def selectPreviousTab() = TabManager.getActiveTab match {
    case Some(active) =>
      val idx = TabManager.openTabs.indexWhere(_._1 == active)
      if (idx == -1 || TabManager.openTabs.length <= 1) {
        TabManager.openTabs.headOption.foreach(x => TabManager.selectTab(x._1))
      } else if (idx == 0) {
        TabManager.selectTab(TabManager.openTabs.last._1)
      } else {
        TabManager.selectTab(TabManager.openTabs(idx - 1)._1)
      }
    case None => TabManager.openTabs.headOption.foreach(x => TabManager.selectTab(x._1))
  }
}
