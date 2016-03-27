import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import ui.{ AdHocQueryManager, EditorManager, SearchManager }
import utils.Logging

import scala.scalajs.js

trait InitHelper { this: DatabaseFlow =>
  protected[this] def init() {
    $("#new-query-link").click({ (e: JQueryEventObject) =>
      AdHocQueryManager.addNewQuery(sendMessage)
      false
    })

    js.Dynamic.global.$(".button-collapse").sideNav()
    js.Dynamic.global.$("select").material_select()

    EditorManager.initEditorFramework()
    SearchManager.init()

    Logging.info("Database Flow Started.")
    connect()
  }
}
