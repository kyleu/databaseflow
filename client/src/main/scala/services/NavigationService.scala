package services

import java.util.UUID

import utils.NullUtils

object NavigationService {
  private[this] lazy val loc = org.scalajs.dom.document.location

  lazy val connectionId = {
    val qIndex = loc.pathname.indexOf("/q/")
    if (qIndex == -1) {
      UUID.fromString("00000000-0000-0000-0000-000000000000")
    } else {
      val s = loc.pathname.substring(qIndex + 3, qIndex + 3 + 36)
      UUID.fromString(s)
    }
  }

  lazy val socketUrl = {
    val wsProtocol = if (loc.protocol == "https:") { "wss" } else { "ws" }
    s"$wsProtocol://${loc.host}/q/$connectionId/websocket"
  }

  def initialMessage = Option(loc.hash).getOrElse("").stripPrefix("#") match {
    case x if x.isEmpty || x == "new" => "new" -> None
    case x if x.startsWith("table-") => "table" -> Some(x.substring(6))
    case x if x.startsWith("view-") => "view" -> Some(x.substring(5))
    case x if x.startsWith("procedure-") => "procedure" -> Some(x.substring(10))
    case x =>
      utils.Logging.warn(s"Unhandled initial action [$x].")
      "new" -> None
  }
}
