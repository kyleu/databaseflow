import models.query.SavedQuery
import models.schema.Schema
import models.{ InitialState, ViewTable }
import models.template.SidenavTemplate
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import utils.Logging

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with MessageHelper with InitHelper {
  val debug = true

  var savedQueries: Option[Seq[SavedQuery]] = None
  var schema: Option[Schema] = None

  lazy val workspace = {
    val r = $("#workspace")
    if (r.length == 0) {
      throw new IllegalStateException("No workspace.")
    }
    r
  }

  init()

  def onInitialState(is: InitialState) = {
    val sch = is.schema

    Logging.info(s"Initial state received containing [${is.savedQueries.size}] saved queries, " +
      s"[${sch.tables.size}] tables, [${sch.procedures.size}] procedures, and [${sch.views.size}] views.")

    savedQueries = Some(is.savedQueries)
    if (is.savedQueries.nonEmpty) {
      $("#saved-query-list-toggle").css("display", "block")
      $("#saved-query-list").html(SidenavTemplate.savedQueries(is.savedQueries).mkString("\n"))
    } else {
      $("#saved-query-list-toggle").css("display", "none")
    }

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
