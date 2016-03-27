import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NavigationService
import ui._
import utils.Logging

import scala.scalajs.js

trait InitHelper { this: DatabaseFlow =>
  protected[this] def init() {
    utils.Logging.installErrorHandler()

    $("#new-query-link").click({ (e: JQueryEventObject) =>
      AdHocQueryManager.addNewQuery(sendMessage)
      false
    })

    js.Dynamic.global.$(".button-collapse").sideNav()
    js.Dynamic.global.$("select").material_select()

    EditorManager.initEditorFramework()
    SearchManager.init()

    Logging.debug("Database Flow has started.")
    connect()
  }

  protected[this] def performInitialAction() = NavigationService.initialMessage match {
    case ("new", None) => AdHocQueryManager.addNewQuery(sendMessage)
    case ("table", Some(id)) => TableManager.tableDetail(id, sendMessage)
    case ("view", Some(id)) => ViewDetailManager.viewDetail(id, sendMessage)
    case ("procedure", Some(id)) => ProcedureDetailManager.procedureDetail(id, sendMessage)
    case (key, id) => utils.Logging.info(s"Unhandled initial message [$key:${id.getOrElse("")}].")
  }
}
