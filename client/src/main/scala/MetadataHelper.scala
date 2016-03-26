import models.ViewTable
import models.query.SavedQuery
import models.schema.Schema
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }

trait MetadataHelper { this: DatabaseFlow =>
  var savedQueries: Option[Seq[SavedQuery]] = None
  var schema: Option[Schema] = None

  def setSavedQueries(sq: Seq[SavedQuery]) = {
    savedQueries = Some(sq)
    if (sq.nonEmpty) {
      $("#saved-query-list-toggle").css("display", "block")
      $("#saved-query-list").html(SidenavTemplate.savedQueries(sq).mkString("\n"))
    } else {
      $("#saved-query-list-toggle").css("display", "none")
    }
  }

  def setSchema(sch: Schema) = {
    schema = Some(sch)
    if (sch.tables.nonEmpty) {
      $("#table-list-toggle").css("display", "block")
      $("#table-list").html(SidenavTemplate.tables(sch).mkString("\n"))
      $(".table-link").click { (e: JQueryEventObject) =>
        val name = e.delegateTarget.id.stripPrefix("table-")
        sendMessage(ViewTable(name))
        false
      }
    } else {
      $("#table-list-toggle").css("display", "none")
    }
    if (sch.views.nonEmpty) {
      $("#view-list-toggle").css("display", "block")
      $("#view-list").html(SidenavTemplate.views(sch).mkString("\n"))
    } else {
      $("#view-list-toggle").css("display", "none")
    }
    if (sch.procedures.nonEmpty) {
      $("#procedure-list-toggle").css("display", "block")
      $("#procedure-list").html(SidenavTemplate.procedures(sch).mkString("\n"))
    } else {
      $("#procedure-list-toggle").css("display", "none")
    }
  }
}
