import java.util.UUID

import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NavigationService
import ui._
import utils.{ Logging, NetworkMessage }

import scala.scalajs.js

trait InitHelper { this: DatabaseFlow =>
  protected[this] def init() {
    utils.Logging.installErrorHandler()

    NetworkMessage.register(sendMessage)

    $("#new-query-link").click({ (e: JQueryEventObject) =>
      AdHocQueryManager.addNewQuery()
      false
    })

    js.Dynamic.global.$(".button-collapse").sideNav()
    js.Dynamic.global.$("select").material_select()

    EditorManager.initEditorFramework()
    SearchManager.init()
    QueryFormManager.init()

    Logging.debug("Database Flow has started.")
    connect()
  }

  protected[this] def performInitialAction() = {
    TabManager.initIfNeeded()
    NavigationService.initialMessage match {
      case ("new", None) => AdHocQueryManager.addNewQuery()
      case ("new", Some(id)) => AdHocQueryManager.addNewQuery(queryId = UUID.fromString(id))
      case ("saved-query", Some(id)) => SavedQueryManager.savedQueryDetail(UUID.fromString(id))
      case ("table", Some(id)) => TableManager.tableDetail(id)
      case ("view", Some(id)) => ViewManager.viewDetail(id)
      case ("procedure", Some(id)) => ProcedureManager.procedureDetail(id)
      case (key, id) => utils.Logging.info(s"Unhandled initial message [$key:${id.getOrElse("")}].")
    }
  }
}
