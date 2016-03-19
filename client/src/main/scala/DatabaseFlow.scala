import models.InitialState
import models.template.SidenavTemplate
import org.scalajs.jquery.{ jQuery => $ }
import utils.Logging

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with MessageHelper with InitHelper {
  val debug = true

  lazy val workspace = {
    val r = $("#workspace")
    if (r.length == 0) {
      throw new IllegalStateException("No workspace.")
    }
    r
  }

  init()

  def onInitialState(is: InitialState) = {
    if (is.schema.tables.nonEmpty) {
      $("#table-list-toggle").css("display", "block")
      $("#table-list").html(SidenavTemplate.tables(is.schema).mkString("\n"))
    } else {
      $("#table-list-toggle").css("display", "None")
    }
    if (is.schema.tables.nonEmpty) {
      $("#view-list-toggle").css("display", "block")
      $("#view-list").html(SidenavTemplate.views(is.schema).mkString("\n"))
    } else {
      $("#view-list-toggle").css("display", "none")
    }
    if (is.schema.tables.nonEmpty) {
      $("#procedure-list-toggle").css("display", "block")
      $("#procedure-list").html(SidenavTemplate.procedures(is.schema).mkString("\n"))
    } else {
      $("#procedure-list-toggle").css("display", "none")
    }
  }
}
