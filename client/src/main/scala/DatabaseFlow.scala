import models.InitialState
import org.scalajs.jquery.{ jQuery => $ }
import services.NotificationService
import ui._

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with InitHelper with ResultsHelper with PlanHelper with MessageHelper {
  val debug = true

  init()

  def onInitialState(is: InitialState) = {
    //utils.Logging.info(s"Initial state received containing [${is.savedQueries.size}] saved queries, " +
    //  s"[${is.schema.tables.size}] tables, [${is.schema.procedures.size}] procedures, and [${is.schema.views.size}] views.")

    UserManager.userId = Some(is.userId)
    UserManager.username = is.username
    UserManager.preferences = Some(is.preferences)

    MetadataManager.setSavedQueries(is.savedQueries, (id) => {
      SavedQueryManager.savedQueryDetail(id)
    })

    MetadataManager.setSchema(is.schema, (key, name) => key match {
      case "table" => TableManager.tableDetail(name)
      case "view" => ViewManager.viewDetail(name)
      case "procedure" => ProcedureManager.procedureDetail(name)
    })

    $("#loading-panel").hide()

    performInitialAction()
  }

  protected[this] def handleServerError(reason: String, content: String) = {
    NotificationService.error(reason, content)
  }
}
