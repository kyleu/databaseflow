package ui

import java.util.UUID

import models.query.SavedQuery
import models.schema.Schema
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

object MetadataManager {
  var savedQueries: Option[Seq[SavedQuery]] = None
  var schema: Option[Schema] = None

  def setSavedQueries(sq: Seq[SavedQuery], onClick: (UUID) => Unit) = {
    savedQueries = Some(sq)
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
  }

  def setSchema(sch: Schema, onClick: (String, String) => Unit) = {
    schema = Some(sch)
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
  }

  def getSavedQuery(id: UUID) = savedQueries.flatMap(_.find(_.id == id))
  def getTable(name: String) = schema.flatMap(_.tables.find(_.name == name))
  def getView(name: String) = schema.flatMap(_.views.find(_.name == name))
  def getProcedure(name: String) = schema.flatMap(_.procedures.find(_.name == name))
}
