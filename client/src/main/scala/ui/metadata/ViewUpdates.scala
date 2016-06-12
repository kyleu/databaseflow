package ui.metadata

import models.schema.View
import models.template.SidenavTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.query.ViewManager
import utils.DomUtils

object ViewUpdates {
  var views: Option[Seq[(String, JQuery, JQuery)]] = None

  def updateViews(updates: Seq[View], fullSchema: Boolean) = {
    val updatedIds = updates.map(_.name)
    val vs = if (fullSchema) {
      updates
    } else {
      val orig = MetadataManager.schema.map(_.views).getOrElse(Nil)
      (orig.filterNot(t => updatedIds.contains(t.name)) ++ updates).sortBy(_.name)
    }

    if (vs.nonEmpty) {
      $("#view-list-toggle").css("display", "block")
      val viewList = $("#view-list")
      viewList.html(SidenavTemplate.views(vs).mkString("\n"))
      utils.JQueryUtils.clickHandler($(".sidenav-link", viewList), (jq) => {
        val name = jq.data("key").toString
        ViewManager.viewDetail(name)
      })
    } else {
      $("#view-list-toggle").css("display", "none")
    }
    views = Some(vs.map { x =>
      val el = $("#view-link-" + DomUtils.cleanForId(x.name))
      (x.name, el, $("span", el))
    })

    MetadataManager.schema = MetadataManager.schema.map(s => s.copy(views = vs))
    ModelListManager.updatePanel("view")
  }
}
