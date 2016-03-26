import models.InitialState
import ui._
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

    MetadataManager.setSavedQueries(is.savedQueries, (id) => {
      val savedQuery = MetadataManager.getSavedQuery(id).getOrElse(throw new IllegalStateException(s"Unknown saved query [$id]."))
      SavedQueryManager.savedQueryDetail(savedQuery, sendMessage)
    })

    MetadataManager.setSchema(is.schema, (key, name) => key match {
      case "table" =>
        val table = MetadataManager.getTable(name).getOrElse(throw new IllegalStateException(s"Unknown table [$name]."))
        TableDetailManager.tableDetail(table, sendMessage)
      case "view" =>
        val view = MetadataManager.getView(name).getOrElse(throw new IllegalStateException(s"Unknown view [$name]."))
        ViewDetailManager.viewDetail(view, sendMessage)
      case "procedure" =>
        val procedure = MetadataManager.getProcedure(name).getOrElse(throw new IllegalStateException(s"Unknown procedure [$name]."))
      //ProcedureDetailManager.procedureDetail(procedure, sendMessage)
    })

    $("#loading-panel").hide()
    QueryManager.addNewQuery(sendMessage)
  }
}
