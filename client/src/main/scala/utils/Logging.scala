package utils

import org.scalajs.dom
import org.scalajs.dom.raw.Event

import scala.scalajs.js.Dynamic.global

object Logging {
  private[this] val showDebug = true

  def logJs(o: scalajs.js.Any) = {
    global.window.debug = o
    global.console.log(o)
  }

  def debug(msg: String) = if (showDebug) {
    global.console.log(msg)
  }
  def info(msg: String) = global.console.info(msg)
  def warn(msg: String) = global.console.warn(msg)
  def error(msg: String) = global.console.error(msg)

  def installErrorHandler() = {
    dom.document.onerror = (e: Event) => {
      info("Script Error Encountered")
      error(e.toString)
      if (showDebug) {
        // rethrow
      }
    }
  }
}
