package ui.metadata

import models.query.RowDataOptions
import models.schema.Table
import models.template.SidenavTemplate
import org.scalajs.jquery.{JQuery, jQuery => $}
import ui.query.TableManager
import util.TemplateUtils

object TableUpdates {
  var tables: Option[Seq[(String, JQuery, JQuery)]] = None

  def updateTables(updates: Seq[Table], fullSchema: Boolean) = {
    val updatedIds = updates.map(_.name)
    val ts = if (fullSchema) {
      updates
    } else {
      val orig = MetadataManager.schema.map(_.tables).getOrElse(Nil)
      (orig.filterNot(t => updatedIds.contains(t.name)) ++ updates).sortBy(_.name)
    }

    if (ts.nonEmpty) {
      $("#table-list-toggle").css("display", "block")
      val tableList = $("#table-list")
      tableList.html(SidenavTemplate.tables(ts).mkString("\n"))
      TemplateUtils.clickHandler($(".sidenav-link", tableList), jq => {
        val name = jq.data("key").toString
        TableManager.tableDetail(name, RowDataOptions.empty)
      })
    } else {
      $("#table-list-toggle").css("display", "none")
    }
    tables = Some(ts.map { x =>
      val el = $("#table-link-" + TemplateUtils.cleanForId(x.name))
      (x.name, el, $("span", el))
    })

    MetadataManager.schema = MetadataManager.schema.map(s => s.copy(tables = ts))
    ModelListManager.updatePanel("table")
  }
}
