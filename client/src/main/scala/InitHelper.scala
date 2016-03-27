import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import ui.{ AdHocQueryManager, SearchManager }
import utils.Logging

import scala.scalajs.js

trait InitHelper { this: DatabaseFlow =>
  protected[this] def init() {
    js.Dynamic.global.ace.require("ace/ext/language_tools")

    $("#new-query-link").click({ (e: JQueryEventObject) =>
      AdHocQueryManager.addNewQuery(sendMessage)
      false
    })

    js.Dynamic.global.$(".button-collapse").sideNav()
    js.Dynamic.global.$("select").material_select()

    SearchManager.init()

    Logging.info("Database Flow Started.")
    connect()
  }
}
