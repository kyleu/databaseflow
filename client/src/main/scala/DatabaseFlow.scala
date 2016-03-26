import models.InitialState
import ui.{ MetadataManager, QueryManager, TableViewManager }
import utils.Logging

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with InitHelper with MessageHelper {
  val debug = true

  init()

  QueryManager.addNewQuery(sendMessage)

  def onInitialState(is: InitialState) = {
    Logging.info(s"Initial state received containing [${is.savedQueries.size}] saved queries, " +
      s"[${is.schema.tables.size}] tables, [${is.schema.procedures.size}] procedures, and [${is.schema.views.size}] views.")

    MetadataManager.setSavedQueries(is.savedQueries)
    MetadataManager.setSchema(is.schema, (name) => {
      val table = MetadataManager.getTable(name).getOrElse(throw new IllegalStateException(s"Unknown table [$name]."))
      TableViewManager.viewTable(table, sendMessage)
    })
  }
}
