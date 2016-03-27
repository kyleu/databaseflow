import models.InitialState
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
      val savedQuery = MetadataManager.getSavedQuery(id).getOrElse(throw new IllegalStateException(s"Unknown saved query [$id]."))
      SavedQueryManager.savedQueryDetail(savedQuery, sendMessage)
    })

    MetadataManager.setSchema(is.schema, (key, name) => key match {
      case "table" => TableManager.tableDetail(name, sendMessage)
      case "view" => ViewDetailManager.viewDetail(name, sendMessage)
      case "procedure" => ProcedureDetailManager.procedureDetail(name, sendMessage)
    })

    $("#loading-panel").hide()
    AdHocQueryManager.addNewQuery(sendMessage)

  }
}
