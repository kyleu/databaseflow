import models.RequestMessage
import utils.Logging

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with MessageHelper {
  val debug = true

  Logging.info("Database Flow Started.")
  connect()
}
