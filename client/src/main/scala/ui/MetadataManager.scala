package ui

import java.util.UUID

import models.engine.DatabaseEngine
import models.query.SavedQuery
import models.schema.Schema
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

object MetadataManager {
  var engine: Option[DatabaseEngine] = None
  var schema: Option[Schema] = None

  var savedQueries: Option[Seq[(String, JQuery, JQuery)]] = None
  var tables: Option[Seq[(String, JQuery, JQuery)]] = None
  var views: Option[Seq[(String, JQuery, JQuery)]] = None
  var procedures: Option[Seq[(String, JQuery, JQuery)]] = None

  def setSavedQueries(sq: Seq[SavedQuery], onClick: (UUID) => Unit) = {
    SavedQueryManager.savedQueries = sq.map(s => s.id -> s).toMap

    if (sq.nonEmpty) {
      $("#saved-query-list-toggle").css("display", "block")
      val savedQueryList = $("#saved-query-list")
      savedQueryList.html(SidenavTemplate.savedQueries(sq).mkString("\n"))
      $(".sidenav-link", savedQueryList).click { (e: JQueryEventObject) =>
        val id = UUID.fromString(e.delegateTarget.id.stripPrefix("sidenav-saved-query-"))
        onClick(id)
        false
      }
    } else {
      $("#saved-query-list-toggle").css("display", "none")
    }

    savedQueries = Some(sq.map { x =>
      val el = $("#sidenav-saved-query-" + x.id)
      (x.id.toString, el, $("span", el))
    })
  }

  def setSchema(sch: Schema, onClick: (String, String) => Unit) = {
    if (sch.tables.nonEmpty) {
      $("#table-list-toggle").css("display", "block")
      val tableList = $("#table-list")
      tableList.html(SidenavTemplate.tables(sch.tables).mkString("\n"))
      $(".sidenav-link", tableList).click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("sidenav-table-")
        onClick("table", name)
        false
      }
    } else {
      $("#table-list-toggle").css("display", "none")
    }
    tables = Some(sch.tables.map { x =>
      val el = $("#sidenav-table-" + x.name)
      (x.name, el, $("span", el))
    })

    if (sch.views.nonEmpty) {
      $("#view-list-toggle").css("display", "block")
      val viewList = $("#view-list")
      viewList.html(SidenavTemplate.views(sch.views).mkString("\n"))
      $(".sidenav-link", viewList).click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("sidenav-view-")
        onClick("view", name)
        false
      }
    } else {
      $("#view-list-toggle").css("display", "none")
    }
    views = Some(sch.views.map { x =>
      val el = $("#sidenav-view-" + x.name)
      (x.name, el, $("span", el))
    })

    if (sch.procedures.nonEmpty) {
      $("#procedure-list-toggle").css("display", "block")
      val procedureList = $("#procedure-list")
      procedureList.html(SidenavTemplate.procedures(sch.procedures).mkString("\n"))
      $(".sidenav-link", procedureList).click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("sidenav-procedure-")
        onClick("procedure", name)
        false
      }
    } else {
      $("#procedure-list-toggle").css("display", "none")
    }
    procedures = Some(sch.procedures.map { x =>
      val el = $("#sidenav-procedure-" + x.name)
      (x.name, el, $("span", el))
    })

    schema = Some(sch)
    engine = Some(DatabaseEngine.get(sch.engine))
  }

  def getSavedQuery(id: String) = savedQueries.flatMap(_.find(_._1 == id)).map(_._1)
  def getTable(name: String) = tables.flatMap(_.find(_._1 == name))
  def getView(name: String) = views.flatMap(_.find(_._1 == name))
  def getProcedure(name: String) = procedures.flatMap(_.find(_._1 == name))
}
