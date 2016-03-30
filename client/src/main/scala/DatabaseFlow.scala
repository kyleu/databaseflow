import models.InitialState
import models.engine.DatabaseEngine
import org.scalajs.jquery.{ jQuery => $ }
import ui._

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with InitHelper with MessageHelper {
  val debug = true

  init()

  def onInitialState(is: InitialState) = {
    //utils.Logging.info(s"Initial state received containing [${is.savedQueries.size}] saved queries, " +
    //  s"[${is.schema.tables.size}] tables, [${is.schema.procedures.size}] procedures, and [${is.schema.views.size}] views.")

    MetadataManager.setSavedQueries(is.savedQueries, (id) => {
      SavedQueryManager.savedQueryDetail(id, sendMessage)
    })

    MetadataManager.setSchema(is.schema, (key, name) => key match {
      case "table" => TableManager.tableDetail(name, sendMessage)
      case "view" => ViewManager.viewDetail(name, sendMessage)
      case "procedure" => ProcedureManager.procedureDetail(name, sendMessage)
    })

    $("#loading-panel").hide()

    performInitialAction()
  }
}
