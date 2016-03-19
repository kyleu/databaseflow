import models.{ Ping, RequestMessage }
import navigation.Navigation
import utils.{ JsonSerializers, Logging, NetworkSocket }

import scala.scalajs.js.timers._

trait NetworkHelper { this: DatabaseFlow =>
  protected[this] val socket = new NetworkSocket(onSocketConnect, onSocketMessage, onSocketError, onSocketClose)

  protected[this] var latencyMs: Option[Int] = None

  protected def connect() = {
    socket.open(Navigation.socketUrl)
  }

  private def sendPing(): Unit = {
    if (socket.connected) {
      sendMessage(Ping(System.currentTimeMillis))
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

  protected[this] def sendMessage(rm: RequestMessage): Unit = {
    if (socket.connected) {
      val json = JsonSerializers.writeRequestMessage(rm, debug)
      socket.send(json)
    } else {
      throw new IllegalStateException("Not connected.")
    }
  }

  protected[this] def onSocketMessage(json: String): Unit = {
    val msg = JsonSerializers.readResponseMessage(json)
    handleMessage(msg)
  }
}
