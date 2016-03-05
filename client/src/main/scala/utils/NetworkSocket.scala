package utils

import org.scalajs.dom.raw._

import scala.scalajs.js
import scala.scalajs.js.JSON

class NetworkSocket(onConnect: () => Unit, onMessage: (String) => Unit, onError: (String) => Unit, onClose: () => Unit) {
  var connecting = false
  var connected = false
  private[this] var ws: Option[WebSocket] = None

  def open(url: String) = if (connected) {
    throw new IllegalStateException("Already connected.")
  } else if (connecting) {
    throw new IllegalStateException("Already connecting.")
  } else {
    openSocket(url)
  }

  def send(s: String): Unit = ws match {
    case Some(socket) => socket.send(s)
    case None => throw new IllegalStateException()
  }

  def send(c: String, v: js.Dynamic): Unit = send(s"""{"c": "$c", "v": ${JSON.stringify(v)} }""")

  private[this] def openSocket(url: String) = {
    connecting = true
    val socket = new WebSocket(url)
    socket.onopen = { (event: Event) => onConnectEvent(event) }
    socket.onerror = { (event: ErrorEvent) => onErrorEvent(event) }
    socket.onmessage = { (event: MessageEvent) => onMessageEvent(event) }
    socket.onclose = { (event: Event) => onCloseEvent(event) }
    ws = Some(socket)
  }

  private[this] def onConnectEvent(event: Event) = {
    connecting = false
    connected = true
    onConnect()
    event
  }

  private[this] def onErrorEvent(event: ErrorEvent) = {
    onError("Websocket error: " + event)
    event
  }

  private[this] def onMessageEvent(event: MessageEvent) = {
    val msg = event.data.toString
    onMessage(msg)
    event
  }

  private[this] def onCloseEvent(event: Event) = {
    connecting = false
    connected = false
    onClose()
    event
  }
}
