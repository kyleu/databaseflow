import services.NotificationService

import scala.scalajs.js.annotation.JSExport

@JSExport
class DatabaseFlow
    extends NetworkHelper
    with InitHelper
    with QueryResultsHelper
    with ModelResultsHelper
    with PlanHelper
    with CacheHelper
    with MessageHelper {
  val debug = true

  init()

  protected[this] def handleServerError(reason: String, content: String) = {
    NotificationService.error(reason, content)
  }
}
