package ui

import models.query.RowDataOptions
import models.schema.{ Procedure, Table, View }
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

object MetadataUpdates {
  def schemaClick(key: String, name: String) = key match {
    case "table" => TableManager.tableDetail(name, RowDataOptions.empty)
    case "view" => ViewManager.viewDetail(name)
    case "procedure" => ProcedureManager.procedureDetail(name)
    case _ => throw new IllegalStateException("Invalid key [" + key + "].")
  }

  var tables: Option[Seq[(String, JQuery, JQuery)]] = None
  var views: Option[Seq[(String, JQuery, JQuery)]] = None
  var procedures: Option[Seq[(String, JQuery, JQuery)]] = None

  def updateTables(updates: Seq[Table]) = {
    val updatedIds = updates.map(_.name)
    val orig = MetadataManager.schema.map(_.tables).getOrElse(Nil)
    val ts = (orig.filterNot(t => updatedIds.contains(t.name)) ++ updates).sortBy(_.name)

    if (ts.nonEmpty) {
      $("#table-list-toggle").css("display", "block")
      val tableList = $("#table-list")
      tableList.html(SidenavTemplate.tables(ts).mkString("\n"))
      $(".sidenav-link", tableList).click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("sidenav-table-")
        schemaClick("table", name)
        false
      }
    } else {
      $("#table-list-toggle").css("display", "none")
    }
    tables = Some(ts.map { x =>
      val el = $("#sidenav-table-" + x.name)
      (x.name, el, $("span", el))
    })

    MetadataManager.schema = MetadataManager.schema.map(s => s.copy(tables = ts))
    ModelListManager.updatePanel("table")
  }

  def updateViews(updates: Seq[View]) = {
    val updatedIds = updates.map(_.name)
    val orig = MetadataManager.schema.map(_.views).getOrElse(Nil)
    val vs = (orig.filterNot(v => updatedIds.contains(v.name)) ++ updates).sortBy(_.name)

    if (vs.nonEmpty) {
      $("#view-list-toggle").css("display", "block")
      val viewList = $("#view-list")
      viewList.html(SidenavTemplate.views(vs).mkString("\n"))
      $(".sidenav-link", viewList).click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("sidenav-view-")
        schemaClick("view", name)
        false
      }
    } else {
      $("#view-list-toggle").css("display", "none")
    }
    views = Some(vs.map { x =>
      val el = $("#sidenav-view-" + x.name)
      (x.name, el, $("span", el))
    })

    MetadataManager.schema = MetadataManager.schema.map(s => s.copy(views = vs))
    ModelListManager.updatePanel("view")
  }

  def updateProcedures(updates: Seq[Procedure]) = {
    val updatedIds = updates.map(_.name)
    val orig = MetadataManager.schema.map(_.procedures).getOrElse(Nil)
    val ps = (orig.filterNot(v => updatedIds.contains(v.name)) ++ updates).sortBy(_.name)

    if (ps.nonEmpty) {
      $("#procedure-list-toggle").css("display", "block")
      val procedureList = $("#procedure-list")
      procedureList.html(SidenavTemplate.procedures(ps).mkString("\n"))
      $(".sidenav-link", procedureList).click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("sidenav-procedure-")
        schemaClick("procedure", name)
        false
      }
    } else {
      $("#procedure-list-toggle").css("display", "none")
    }
    procedures = Some(ps.map { x =>
      val el = $("#sidenav-view-" + x.name)
      (x.name, el, $("span", el))
    })

    MetadataManager.schema = MetadataManager.schema.map(s => s.copy(procedures = ps))
    ModelListManager.updatePanel("procedure")
  }
}
