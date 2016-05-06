import services.{ InitService, NotificationService }

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow extends NetworkHelper with MessageHelper {
  val debug = true

  InitService.init(sendMessage, connect)

  protected[this] def handleServerError(reason: String, content: String) = {
    NotificationService.error(reason, content)
  }
}
