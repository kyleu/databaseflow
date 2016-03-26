import models.InitialState
import ui.{ MetadataManager, QueryManager, TableDetailManager }
import utils.Logging
import org.scalajs.jquery.{ jQuery => $ }

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with InitHelper with MessageHelper {
  val debug = true

  init()

  def onInitialState(is: InitialState) = {
    Logging.info(s"Initial state received containing [${is.savedQueries.size}] saved queries, " +
      s"[${is.schema.tables.size}] tables, [${is.schema.procedures.size}] procedures, and [${is.schema.views.size}] views.")

    MetadataManager.setSavedQueries(is.savedQueries)
    MetadataManager.setSchema(is.schema, (key, name) => key match {
      case "table" =>
        val table = MetadataManager.getTable(name).getOrElse(throw new IllegalStateException(s"Unknown table [$name]."))
        TableDetailManager.viewTable(table, sendMessage)
      case "view" =>
        val view = MetadataManager.getView(name).getOrElse(throw new IllegalStateException(s"Unknown view [$name]."))

      case "procedure" =>
        val procedure = MetadataManager.getProcedure(name).getOrElse(throw new IllegalStateException(s"Unknown procedure [$name]."))

    })

    $("#loading-panel").hide()
    QueryManager.addNewQuery(sendMessage)
  }
}
