package ui

import java.util.UUID

import models.query.SavedQuery
import models.schema.{ Procedure, Schema, Table }
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

object MetadataManager {
  var savedQueries: Option[Seq[(SavedQuery, JQuery)]] = None

  var schema: Option[Schema] = None
  var tables: Option[Seq[(Table, JQuery)]] = None
  var views: Option[Seq[(Table, JQuery)]] = None
  var procedures: Option[Seq[(Procedure, JQuery)]] = None

  def setSavedQueries(sq: Seq[SavedQuery], onClick: (UUID) => Unit) = {
    if (sq.nonEmpty) {
      $("#saved-query-list-toggle").css("display", "block")
      $("#saved-query-list").html(SidenavTemplate.savedQueries(sq).mkString("\n"))
      $(".saved-query-link").click { (e: JQueryEventObject) =>
        val id = UUID.fromString(e.delegateTarget.id.stripPrefix("saved-query-"))
        onClick(id)
        true
      }
    } else {
      $("#saved-query-list-toggle").css("display", "none")
    }
    val ret = sq.map(x => x -> $("#saved-query-" + x.id))
    savedQueries = Some(ret)
  }

  def setSchema(sch: Schema, onClick: (String, String) => Unit) = {
    if (sch.tables.nonEmpty) {
      $("#table-list-toggle").css("display", "block")
      $("#table-list").html(SidenavTemplate.tables(sch).mkString("\n"))
      $(".table-link").click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("table-")
        onClick("table", name)
        true
      }
    } else {
      $("#table-list-toggle").css("display", "none")
    }
    tables = Some(sch.tables.map(x => x -> $("#table-" + x.name)))

    if (sch.views.nonEmpty) {
      $("#view-list-toggle").css("display", "block")
      $("#view-list").html(SidenavTemplate.views(sch).mkString("\n"))
      $(".view-link").click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("view-")
        onClick("view", name)
        true
      }
    } else {
      $("#view-list-toggle").css("display", "none")
    }
    views = Some(sch.views.map(x => x -> $("#view-" + x.name)))

    if (sch.procedures.nonEmpty) {
      $("#procedure-list-toggle").css("display", "block")
      $("#procedure-list").html(SidenavTemplate.procedures(sch).mkString("\n"))
      $(".procedure-link").click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("procedure-")
        onClick("procedure", name)
        true
      }
    } else {
      $("#procedure-list-toggle").css("display", "none")
    }
    procedures = Some(sch.procedures.map(x => x -> $("#procedure-" + x.name)))

    schema = Some(sch)
  }

  def getSavedQuery(id: UUID) = savedQueries.flatMap(_.find(_._1.id == id)).map(_._1)
  def getTable(name: String) = schema.flatMap(_.tables.find(_.name == name))
  def getView(name: String) = schema.flatMap(_.views.find(_.name == name))
  def getProcedure(name: String) = schema.flatMap(_.procedures.find(_.name == name))
}
