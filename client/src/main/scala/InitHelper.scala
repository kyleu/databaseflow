import java.util.UUID

import models.query.RowDataOptions
import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import services.NavigationService
import ui._
import utils.{ Logging, NetworkMessage }

import scala.scalajs.js

trait InitHelper { this: DatabaseFlow =>
  protected[this] def init() {
    utils.Logging.installErrorHandler()

    NetworkMessage.register(sendMessage)

    wireSideNav()

    js.Dynamic.global.$("select").material_select()

    EditorManager.initEditorFramework()
    SearchManager.init()
    ConfirmManager.init()
    QueryFormManager.init()

    Logging.debug("Database Flow has started.")
    connect()
  }

  private[this] def wireSideNav() = {
    $("#new-query-link").click({ (e: JQueryEventObject) =>
      AdHocQueryManager.addNewQuery()
      false
    })

    $(".show-list-link").click({ (e: JQueryEventObject) =>
      val key = $(e.delegateTarget).data("key").toString
      ModelListManager.showList(key)
      false
    })

    js.Dynamic.global.$(".button-collapse").sideNav()
  }

  protected[this] def performInitialAction() = {
    TabManager.initIfNeeded()
    NavigationService.initialMessage match {
      case ("new", None) => AdHocQueryManager.addNewQuery()
      case ("new", Some(id)) => AdHocQueryManager.addNewQuery(queryId = UUID.fromString(id))
      case ("saved-query", Some(id)) => SavedQueryManager.savedQueryDetail(UUID.fromString(id))
      case ("table", Some(id)) => id.indexOf("::") match {
        case -1 => TableManager.tableDetail(id, RowDataOptions.empty)
        case x =>
          val name = id.substring(0, x)
          val filter = id.substring(x + 2).split('=')
          val options = if (filter.length > 1) {
            RowDataOptions(
              filterCol = filter.headOption,
              filterOp = Some("="),
              filterVal = Some(filter.tail.mkString("="))
            )
          } else {
            utils.Logging.info(s"Unable to parse filter [${filter.mkString("=")}].")
            RowDataOptions.empty
          }
          TableManager.tableDetail(name, options)
      }
      case ("view", Some(id)) => ViewManager.viewDetail(id)
      case ("procedure", Some(id)) => ProcedureManager.procedureDetail(id)
      case ("list", Some(key)) => ModelListManager.showList(key)
      case (key, id) =>
        utils.Logging.info(s"Unhandled initial message [$key:${id.getOrElse("")}].")
        AdHocQueryManager.addNewQuery()
    }
  }
}
