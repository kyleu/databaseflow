import models.InitialState
import org.scalajs.jquery.{ jQuery => $ }
import utils.Logging

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with InitHelper with MessageHelper with MetadataHelper with QueryHelper {
  val debug = true

  lazy val workspace = {
    val r = $("#workspace")
    if (r.length == 0) {
      throw new IllegalStateException("No workspace.")
    }
    r
  }

  init()

  addNewQuery()

  def onInitialState(is: InitialState) = {
    Logging.info(s"Initial state received containing [${is.savedQueries.size}] saved queries, " +
      s"[${is.schema.tables.size}] tables, [${is.schema.procedures.size}] procedures, and [${is.schema.views.size}] views.")

    setSavedQueries(is.savedQueries)
    setSchema(is.schema)
  }
}
