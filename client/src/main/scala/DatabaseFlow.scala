import java.util.UUID

import models.user.UserPreferences
import models.{ InitialState, RequestMessage }
import org.scalajs.jquery.{ jQuery => $ }
import ui._

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with InitHelper with MessageHelper {
  val debug = true
  var userId: Option[UUID] = None
  var username: Option[String] = None
  var preferences: Option[UserPreferences] = None

  init()

  def onInitialState(is: InitialState) = {
    //utils.Logging.info(s"Initial state received containing [${is.savedQueries.size}] saved queries, " +
    //  s"[${is.schema.tables.size}] tables, [${is.schema.procedures.size}] procedures, and [${is.schema.views.size}] views.")

    userId = Some(is.userId)
    username = is.username
    preferences = Some(is.preferences)

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
}
