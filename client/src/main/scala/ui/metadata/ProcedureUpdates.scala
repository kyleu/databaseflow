package ui.metadata

import models.schema.Procedure
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQuery, jQuery => $ }
import ui.{ MetadataManager, ProcedureManager }
import utils.DomUtils

object ProcedureUpdates {
  var procedures: Option[Seq[(String, JQuery, JQuery)]] = None

  def updateProcedures(updates: Seq[Procedure], fullSchema: Boolean) = {
    val updatedIds = updates.map(_.name)
    val ps = if (fullSchema) {
      updates
    } else {
      val orig = MetadataManager.schema.map(_.procedures).getOrElse(Nil)
      (orig.filterNot(v => updatedIds.contains(v.name)) ++ updates).sortBy(_.name)
    }

    if (ps.nonEmpty) {
      $("#procedure-list-toggle").css("display", "block")
      val procedureList = $("#procedure-list")
      procedureList.html(SidenavTemplate.procedures(ps).mkString("\n"))
      utils.JQueryUtils.clickHandler($(".sidenav-link", procedureList), (jq) => {
        val name = jq.data("key").toString
        ProcedureManager.procedureDetail(name)
      })
    } else {
      $("#procedure-list-toggle").css("display", "none")
    }
    procedures = Some(ps.map { x =>
      val el = $("#procedure-link-" + DomUtils.cleanForId(x.name))
      (x.name, el, $("span", el))
    })

    MetadataManager.schema = MetadataManager.schema.map(s => s.copy(procedures = ps))
    ModelListManager.updatePanel("procedure")
  }
}
