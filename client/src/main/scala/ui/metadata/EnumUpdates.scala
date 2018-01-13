package ui.metadata

import models.schema.{EnumType, Procedure}
import models.template.SidenavTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.query.{EnumManager, ProcedureManager}
import util.TemplateUtils

object EnumUpdates {
  var enums: Option[Seq[(String, JQuery, JQuery)]] = None

  def updateEnums(updates: Seq[EnumType], fullSchema: Boolean) = {
    val updatedIds = updates.map(_.key)
    val es = if (fullSchema) {
      updates
    } else {
      val orig = MetadataManager.schema.map(_.enums).getOrElse(Nil)
      (orig.filterNot(v => updatedIds.contains(v.key)) ++ updates).sortBy(_.key)
    }

    if (es.nonEmpty) {
      $("#enum-list-toggle").css("display", "block")
      val enumList = $("#enum-list")
      enumList.html(SidenavTemplate.enums(es).mkString("\n"))
      TemplateUtils.clickHandler($(".sidenav-link", enumList), jq => {
        val name = jq.data("key").toString
        EnumManager.enumDetail(name)
      })
    } else {
      $("#enum-list-toggle").css("display", "none")
    }
    enums = Some(es.map { x =>
      val el = $("#enum-link-" + TemplateUtils.cleanForId(x.key))
      (x.key, el, $("span", el))
    })

    MetadataManager.schema = MetadataManager.schema.map(s => s.copy(enums = es))
    ModelListManager.updatePanel("procedure")
  }
}
