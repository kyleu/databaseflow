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
    $("#table-list").html(SidenavTemplate.tables(is.schema).mkString("\n"))
    $("#view-list").html(SidenavTemplate.views(is.schema).mkString("\n"))
    $("#procedure-list").html(SidenavTemplate.procedures(is.schema).mkString("\n"))
  }
}
