package navigation

import java.util.UUID

object Navigation {
  private[this] lazy val loc = org.scalajs.dom.document.location

  lazy val connectionId = {
    val qIndex = loc.pathname.indexOf("/q/")
    if(qIndex == -1) {
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
}
