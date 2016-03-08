import navigation.Navigation
import utils.{ Logging, NetworkSocket }

import scala.scalajs.js.timers._

trait NetworkHelper { this: DatabaseFlow =>
  protected[this] val socket = new NetworkSocket(onSocketConnect, onSocketMessage, onSocketError, onSocketClose)
  socket.open(Navigation.socketUrl)

  private def sendPing(): Unit = {
    if (socket.connected) {
      socket.send(s"""{ "c": "Ping", "v": { "timestamp": ${System.currentTimeMillis} } }""")
    }
    setTimeout(10000)(sendPing())
  }

  setTimeout(1000)(sendPing())

  protected[this] def onSocketConnect(): Unit = {
    Logging.info(s"Socket connected.")
  }

  protected[this] def onSocketError(error: String): Unit = {
    Logging.info(s"Socket error [$error].")
  }

  protected[this] def onSocketClose(): Unit = {
    Logging.info("Socket closed.")
  }

  protected[this] def onSocketMessage(s: String): Unit = {
    Logging.info(s"Socket message [$s].")
  }
}
