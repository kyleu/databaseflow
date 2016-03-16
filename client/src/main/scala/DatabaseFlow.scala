import models.{ InitialState, RequestMessage }
import utils.Logging

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with MessageHelper with EventHelper {
  val debug = true

  Logging.info("Database Flow Started.")
  connect()

  wireEvents()

  def onInitialState(is: InitialState) = {
    Logging.info(is.toString)
  }
}
