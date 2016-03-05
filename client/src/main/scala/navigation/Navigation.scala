package navigation

import java.util.UUID

object Navigation {
  lazy val connectionId = UUID.fromString("00000000-0000-0000-0000-000000000000")

  lazy val socketUrl = {
    val loc = org.scalajs.dom.document.location
    val wsProtocol = if (loc.protocol == "https:") { "wss" } else { "ws" }
    s"$wsProtocol://${loc.host}/q/$connectionId/websocket"
  }
}
