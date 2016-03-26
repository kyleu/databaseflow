import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import ui.QueryManager
import utils.Logging

import scala.scalajs.js

trait InitHelper { this: DatabaseFlow =>
  protected[this] def init() {
    wireEvents()

    Logging.info("Database Flow Started.")
    connect()
  }

  private[this] def wireEvents() = {
    js.Dynamic.global.ace.require("ace/ext/language_tools")

    $("#new-query-link").click({ (e: JQueryEventObject) =>
      QueryManager.addNewQuery(sendMessage)
      false
    })
  }
}
